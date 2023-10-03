package com.example.beactive.activites

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beactive.R
import com.example.beactive.adapters.MemberListItemsAdapter
import com.example.beactive.firebase.FirestoreClass
import com.example.beactive.model.User
import com.example.beactive.models.Event
import com.example.beactive.utils.Constants


class MembersActivity : BaseActivity() {

    private lateinit var mEventDetails: Event
    private lateinit var rvMembersList: RecyclerView
    private lateinit var mAssignedMemberList: ArrayList<User>
    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.EVENT_DETAIL)) {
            mEventDetails = intent.getParcelableExtra<Event>(Constants.EVENT_DETAIL)!!
        }

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this, mEventDetails.assignedTo
        )

    }

    fun setupMembersList(list: ArrayList<User>) {

        mAssignedMemberList = list
        hideProgressDialog()

        rvMembersList = findViewById(R.id.rv_members_list)
        rvMembersList.layoutManager = LinearLayoutManager(this)
        rvMembersList.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rvMembersList.adapter = adapter
    }

    fun memberDetails(user: User) {
        mEventDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToEvent(this, mEventDetails, user)
    }

    private fun setupActionBar() {
        val toolbarMemberActivity: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.toolbar_members_activity)
        setSupportActionBar(toolbarMemberActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        toolbarMemberActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)

        val etEmailEditText = dialog.findViewById<EditText>(R.id.et_email_search_member)
        val tvAdd = dialog.findViewById<TextView>(R.id.tv_add)
        val tvCancel = dialog.findViewById<TextView>(R.id.tv_cancel)

        tvAdd.setOnClickListener {
            val email = etEmailEditText.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter member email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMemberList.add(user)

        anyChangesMade = true

        setupMembersList(mAssignedMemberList)
    }

}



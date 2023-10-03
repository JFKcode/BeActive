package com.example.beactive.activites

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beactive.R
import com.example.beactive.adapters.TaskListItemsAdapter
import com.example.beactive.firebase.FirestoreClass
import com.example.beactive.models.Card
import com.example.beactive.models.Event
import com.example.beactive.models.Task
import com.example.beactive.utils.Constants

class TaskEventListActivity : BaseActivity() {

    private lateinit var mEventDetails: Event
    private lateinit var mEventDocumentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mEventDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getEventDetails(this, mEventDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE ||
            requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getEventDetails(this, mEventDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.EVENT_DETAIL, mEventDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.EVENT_DETAIL, mEventDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        val toolbarTaskListActivity: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mEventDetails.name
        }

        toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun eventDetails(event: Event){
        mEventDetails = event
        hideProgressDialog()
        setupActionBar()

        val rvTaskList = findViewById<RecyclerView>(R.id.rv_task_list)

        val addTaskList = Task(resources.getString(R.string.add_game))
        event.eventList.add(addTaskList)
        rvTaskList.layoutManager= LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false)
        rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, event.eventList)
        rvTaskList.adapter = adapter

    }

    fun addUpdateTaskEventListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getEventDetails(this, mEventDetails.documentId)
    }

    fun createTaskEventList(taskEventListName: String){
        val task = Task(taskEventListName, FirestoreClass().getCurrentUserID())
        mEventDetails.eventList.add(0, task)
        mEventDetails.eventList.removeAt(mEventDetails.eventList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskEventList(this, mEventDetails)
    }

    fun updateTaskEventList(position:Int, listName: String, model: Task){
        val task = Task(listName, model.createdBy)

        mEventDetails.eventList[position] = task
        mEventDetails.eventList.removeAt(mEventDetails.eventList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskEventList(this, mEventDetails)

    }

    fun deleteTaskEventList(position: Int){
        mEventDetails.eventList.removeAt(position)
        mEventDetails.eventList.removeAt(mEventDetails.eventList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskEventList(this, mEventDetails)

    }

    fun addCardToTaskList(position: Int, cardName: String){

        mEventDetails.eventList.removeAt(mEventDetails.eventList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mEventDetails.eventList[position].cards
        cardsList.add(card)

        val task = Task(
            mEventDetails.eventList[position].title,
            mEventDetails.eventList[position].createdBy,
            cardsList
        )

        mEventDetails.eventList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskEventList(this, mEventDetails)

    }

    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE : Int = 14
    }
}

package com.example.beactive.activites

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.beactive.R
import com.example.beactive.firebase.FirestoreClass
import com.example.beactive.models.Card
import com.example.beactive.models.Event
import com.example.beactive.models.Task
import com.example.beactive.utils.Constants

class CardDetailsActivity : BaseActivity() {

    private lateinit var mEventDetails : Event
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private lateinit var etNameCardDetails: EditText
    private lateinit var btnUpdateCardDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()

        etNameCardDetails = findViewById(R.id.et_name_card_details)
        btnUpdateCardDetails = findViewById(R.id.btn_update_card_details)

        etNameCardDetails.setText(mEventDetails
            .eventList[mTaskListPosition]
            .cards[mCardPosition].name)
        etNameCardDetails.setSelection(etNameCardDetails.text.length)

        btnUpdateCardDetails.setOnClickListener {
            if(etNameCardDetails.text.toString().isNotEmpty())
                updateCardDetails()
            else{
                Toast.makeText(this@CardDetailsActivity,
                    "Enter a card name.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun addUpdateTaskEventListSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()

    }

    private fun setupActionBar() {
        val toolbarCardActivity: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.toolbar_card_details_activity )
        setSupportActionBar(toolbarCardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mEventDetails.eventList[mTaskListPosition]
                .cards[mCardPosition].name
        }

        toolbarCardActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mEventDetails
                    .eventList[mTaskListPosition].cards[mCardPosition].name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.EVENT_DETAIL)){
            mEventDetails = intent.getParcelableExtra(Constants.EVENT_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(
                Constants.CARD_LIST_ITEM_POSITION, -1)
        }
    }

    private fun updateCardDetails(){
        val card = Card(
            etNameCardDetails.text.toString(),
            mEventDetails.eventList[mTaskListPosition].cards[mCardPosition].createdBy,
            mEventDetails.eventList[mTaskListPosition].cards[mCardPosition].assignedTo
        )

        mEventDetails.eventList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskEventList(this@CardDetailsActivity, mEventDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mEventDetails
            .eventList[mTaskListPosition].cards

        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mEventDetails.eventList
        taskList.removeAt(taskList.size- 1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskEventList(this@CardDetailsActivity, mEventDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)


        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}



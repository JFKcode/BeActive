package com.example.beactive.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.beactive.activites.CardDetailsActivity
import com.example.beactive.activites.CreateEventActivity
import com.example.beactive.activites.MainActivity
import com.example.beactive.activites.MembersActivity
import com.example.beactive.activites.MyProfileActivity
import com.example.beactive.activites.SignInActivity
import com.example.beactive.activites.SignUpActivity
import com.example.beactive.activites.TaskEventListActivity
import com.example.beactive.model.User
import com.example.beactive.models.Event
import com.example.beactive.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                )
            }
    }

    fun getEventDetails(activity: TaskEventListActivity, documentId : String){
        mFireStore.collection(Constants.EVENTS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val event = document.toObject(Event::class.java)!!
                event.documentId = document.id
                activity.eventDetails(event)

            }.addOnFailureListener{ e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a event.", e)
            }
    }

    fun createEvent(activity: CreateEventActivity, event: Event){
        mFireStore.collection(Constants.EVENTS)
            .document()
            .set(event, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Event created successfully.")
                Toast.makeText(activity, "Event created successfully.",
                    Toast.LENGTH_SHORT).show()
                activity.eventCreatedSuccessfully()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a event."
                )

            }
    }

    fun getEventsList(activity: MainActivity){
        mFireStore.collection(Constants.EVENTS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val eventList: ArrayList<Event> = ArrayList()
                for(i in document.documents){
                    val event = i.toObject(Event::class.java)!!
                    event.documentId = i.id
                    eventList.add(event)
                }
                activity.populateEventsListToUI(eventList)
            }.addOnFailureListener{ e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a event.", e)
            }
    }

    fun addUpdateTaskEventList(activity: Activity, event: Event){
        val taskEventListHashMap = HashMap<String, Any>()
        taskEventListHashMap[Constants.EVENT_LIST] = event.eventList
        mFireStore.collection(Constants.EVENTS)
            .document(event.documentId)
            .update(taskEventListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully")
                if(activity is TaskEventListActivity)
                    activity.addUpdateTaskEventListSuccess()
                else if (activity is CardDetailsActivity)
                    activity.addUpdateTaskEventListSuccess()
            }.addOnFailureListener {
                exception ->
                if(activity is TaskEventListActivity)
                    activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.",exception )

            }
    }

    fun updateUserProfileData(activity: MyProfileActivity,
                              userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Profile Data update successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnSuccessListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.")
                Toast.makeText(activity, "Error when update!", Toast.LENGTH_SHORT).show()

            }
    }

    fun loadUserData(activity: Activity, readEventsList: Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity){
                    is SignInActivity ->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity ->{
                        activity.updateNavigationUserDetails(loggedInUser, readEventsList)
                    }
                    is MyProfileActivity ->{
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener {
                    e ->

                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser", "Error writing document",)
            }
    }

    fun getCurrentUserID(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getAssignedMembersListDetails(
        activity: MembersActivity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                activity.setupMembersList(usersList)
            }.addOnFailureListener { e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )

            }

    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
    }

    fun assignMemberToEvent(
        activity: MembersActivity, event: Event, user: User){

        val assignedToHashMap   = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = event.assignedTo

        mFireStore.collection(Constants.EVENTS)
            .document(event.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
    }


}
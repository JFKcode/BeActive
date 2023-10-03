package com.example.beactive.activites

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beactive.R
import com.example.beactive.adapters.EventItemsAdapter
import com.example.beactive.firebase.FirestoreClass
import com.example.beactive.model.User
import com.example.beactive.models.Event
import com.example.beactive.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val TAG = "MainActivity" // Utwórz stałą TAG dla logów
    private lateinit var drawerLayout: DrawerLayout

    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var rvEventsList: RecyclerView
    private lateinit var tvNoEventAvailable: TextView
    private lateinit var eventsList: ArrayList<com.example.beactive.models.Event>
    private lateinit var adapter: EventItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventsList = ArrayList()

        drawerLayout = findViewById(R.id.drawer_layout)

        setupActionBar()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this, true)

        val fabCreateBoard = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener{
            val intent = Intent(this,
                CreateEventActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
        // Dodaj log, aby śledzić, kiedy aktywność została utworzona
        Log.d(TAG, "Aktywność MainActivity została utworzona")
    }

    fun populateEventsListToUI(eventList: ArrayList<com.example.beactive.models.Event>){
        hideProgressDialog()

        val rvEventsList = findViewById<RecyclerView>(R.id.rv_events_list)
        val tvNoEventAvailable = findViewById<TextView>(R.id.tv_no_event_available)

        if(eventList.size > 0){
            rvEventsList.visibility = View.VISIBLE
            tvNoEventAvailable.visibility = View.GONE

            rvEventsList.layoutManager = LinearLayoutManager(this)
            rvEventsList.setHasFixedSize(true)

            val adapter = EventItemsAdapter(this, eventList)
            rvEventsList.adapter = adapter

            adapter.setOnClickListener(object: EventItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Event) {
                    val intent = Intent(this@MainActivity, TaskEventListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        }else{
            rvEventsList.visibility = View.GONE
            tvNoEventAvailable.visibility = View.VISIBLE
        }

    }

    private fun setupActionBar() {
        val toolbar_main_activity: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onResume() {
        super.onResume()
        // Dodaj log, aby śledzić, kiedy aktywność wchodzi w stan onResume
        Log.d(TAG, "Aktywność MainActivity wchodzi w stan onResume")
    }

    override fun onPause() {
        super.onPause()
        // Dodaj log, aby śledzić, kiedy aktywność wchodzi w stan onPause
        Log.d(TAG, "Aktywność MainActivity wchodzi w stan onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dodaj log, aby śledzić, kiedy aktywność zostaje zniszczona
        Log.d(TAG, "Aktywność MainActivity została zniszczona")
    }

    fun updateNavigationUserDetails(user: User, readEventsList: Boolean){

        mUserName = user.name

        val navUserImage = findViewById<ImageView>(R.id.nav_user_image)
        val tvUsername = findViewById<TextView>(R.id.tv_username)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage);

        tvUsername.text = user.name
        if(readEventsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getEventsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getEventsList(this)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this,
                        MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }
}




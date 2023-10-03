package com.example.beactive.activites

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.beactive.R
import com.example.beactive.firebase.FirestoreClass
import com.example.beactive.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setupActionBar()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar)

        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)

    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this, "you have " +
                    "succesfull registred", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()

    }

    private fun setupActionBar() {

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        val btn_sing_up = findViewById<Button>(R.id.btn_sign_up)
        btn_sing_up.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name: String = etName.text.toString().trim { it <= ' '}
        val email: String = etEmail.text.toString().trim { it <= ' '}
        val password: String = etPassword.text.toString().trim { it <= ' '}

        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message, Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun getFileExtension(mSelectedImageFileUri: Uri) {

    }

    private fun validateForm(name: String,
                             email: String, password: String) : Boolean {
        return when {
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)-> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a name")
                false
            }else -> {
                true
            }
        }
    }
}

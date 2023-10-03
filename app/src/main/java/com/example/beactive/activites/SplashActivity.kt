package com.example.beactive.activites

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log // Dodaj import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beactive.R
import com.example.beactive.firebase.FirestoreClass

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity" // Utwórz stałą TAG dla logów

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        val textApp: TextView = findViewById(R.id.tv_app_name)
        textApp.typeface = typeFace

        // Dodaj logi, aby śledzić przebieg działania aktywności
        Log.d(TAG, "Aktywność SplashActivity została utworzona")

        Handler().postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserID()
            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2500)
    }

    override fun onResume() {
        super.onResume()
        // Dodaj logi, aby śledzić wejście w stan onResume
        Log.d(TAG, "Aktywność SplashActivity wchodzi w stan onResume")
    }

    override fun onPause() {
        super.onPause()
        // Dodaj logi, aby śledzić wejście w stan onPause
        Log.d(TAG, "Aktywność SplashActivity wchodzi w stan onPause")
    }
}
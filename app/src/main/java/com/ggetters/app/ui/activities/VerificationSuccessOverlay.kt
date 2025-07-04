package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class VerificationSuccessOverlay : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age_verification_success)

        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            // TODO: Backend - Log verification success event
            startActivity(Intent(this, WelcomeActivity::class.java))
            finishAffinity()
        }
    }
} 
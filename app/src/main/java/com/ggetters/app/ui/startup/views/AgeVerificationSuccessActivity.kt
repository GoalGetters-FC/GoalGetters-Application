package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class AgeVerificationSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age_verification_success)

        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age verification success
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
} 
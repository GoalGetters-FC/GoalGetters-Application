package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class AgeVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age_verification)

        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)

        verifyButton.setOnClickListener {
            // TODO: Backend - Handle age verification logic
            // On success, always return to SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
} 
package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val continueButton = findViewById<Button>(R.id.continueButton)
        // TODO: Optionally prompt for profile completion
        // TODO: Log analytics event for welcome
        continueButton.setOnClickListener {
            // TODO: Navigate to Dashboard/Home
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
} 
package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.shared.MainActivity

class WelcomeBackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_back)

        val continueButton = findViewById<Button>(R.id.continueButton)
        // TODO: Show summary/tips for returning user
        // TODO: Log analytics event for welcome back
        continueButton.setOnClickListener {
            // TODO: Navigate to Dashboard/Home
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
} 
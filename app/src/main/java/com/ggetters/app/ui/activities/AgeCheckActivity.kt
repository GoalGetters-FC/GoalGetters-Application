package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.auth.AgeVerificationActivity

class AgeCheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age_check)

        val yesButton = findViewById<Button>(R.id.yesButton)
        val noButton = findViewById<Button>(R.id.noButton)

        yesButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (over 18)
            startActivity(Intent(this, AgeVerificationActivity::class.java))
            finish()
        }
        noButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (under 18)
            startActivity(Intent(this, AccessDeniedActivity::class.java))
            finish()
        }
    }
} 
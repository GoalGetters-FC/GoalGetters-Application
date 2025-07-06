package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.startup.dialogs.AgeNoticeBottomSheet

class AgeCheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age_check)

        val yesButton = findViewById<Button>(R.id.yesButton)
        val noButton = findViewById<Button>(R.id.noButton)

        yesButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (over 18)
            startActivity(Intent(this, VerificationActivity::class.java))
            finish()
        }
        noButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (under 18)
            AgeNoticeBottomSheet().show(supportFragmentManager, "AgeNoticeBottomSheet")
        }
    }
} 
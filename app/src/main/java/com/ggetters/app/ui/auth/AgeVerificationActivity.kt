package com.ggetters.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.core.extensions.showFeedbackBottomSheet
import com.ggetters.app.ui.shared.MainActivity

class AgeVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.age_verification_activity)

        val yesButton = findViewById<Button>(R.id.yesButton)
        val noButton = findViewById<Button>(R.id.noButton)

        yesButton.setOnClickListener {
            // TODO: Implement age verification logic and analytics
            // On success:
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        noButton.setOnClickListener {
            // TODO: Show bottom sheet for underage message
            showFeedbackBottomSheet("Access Denied", "You must be 18 or older to use this app.")
        }
    }
}
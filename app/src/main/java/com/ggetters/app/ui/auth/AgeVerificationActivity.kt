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
            // TODO: Backend - Verify user age
            // Endpoint: POST /api/age/verify
            // Request: { userId: String, age: Int }
            // Response: { success: Boolean, requiresParentalConsent: Boolean }
            // Error: { message: String }
            // Notes: If under 18, require parental consent flow.
            // TODO: Backend - Log analytics event for age verification
            // On success:
            val intent = Intent(this, MainActivity::class.java) // Or OnboardingActivity if onboarding is required next
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        noButton.setOnClickListener {
            // TODO: Show bottom sheet for underage message
            // TODO: Backend - Log analytics event for underage access denied
            showFeedbackBottomSheet("Access Denied", "You must be 18 or older to use this app.")
        }
    }
}
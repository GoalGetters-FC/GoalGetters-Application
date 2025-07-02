package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.dialogs.VerificationSuccessDialog

class VerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val submitButton = findViewById<Button>(R.id.submitButton)
        // TODO: Get code input from EditText(s)
        // TODO: Backend - Verify OTP code
        // Endpoint: POST /api/auth/verify-otp
        // Request: { userId: String, otp: String }
        // Response: { success: Boolean }
        // Error: { message: String }
        // Notes: Handle expired/invalid OTP, allow resend.
        // TODO: Backend - Resend OTP
        // Endpoint: POST /api/auth/resend-otp
        // Request: { userId: String }
        // Response: { success: Boolean }
        // TODO: Backend - Log analytics event for verification attempt
        // TODO: Show error bottom sheet on failure
        // On success:
        submitButton.setOnClickListener {
            // TODO: Check if user is new or returning
            val dialog = VerificationSuccessDialog(this)
            dialog.setOnDismissListener {
                // TODO: If new user, go to WelcomeActivity; if returning, go to WelcomeBackActivity
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            dialog.show()
        }
    }
} 
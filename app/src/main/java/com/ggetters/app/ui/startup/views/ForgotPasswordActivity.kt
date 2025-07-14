package com.ggetters.app.ui.startup.views

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        val emailEditText = findViewById<EditText>(R.id.et_identity)
        val sendResetButton = findViewById<Button>(R.id.sendResetButton)
        sendResetButton.setOnClickListener {
            // TODO: Backend - Send password reset email
            // Endpoint: POST /api/auth/forgot-password
            // Request: { email: String }
            // Response: { success: Boolean }
            // Error: { message: String }
            // Notes: Handle non-existent email gracefully.
            // TODO: Backend - Log analytics event for password reset request
            // Example confirmation:
            // showFeedbackBottomSheet("Reset Email Sent", "Check your inbox for a password reset link.")
        }
    }
} 
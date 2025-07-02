package com.ggetters.app.ui.views

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.core.extensions.showFeedbackBottomSheet

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        val sendResetButton = findViewById<Button>(R.id.sendResetButton)
        sendResetButton.setOnClickListener {
            // TODO: Implement Firebase Auth password reset and analytics
            // Example confirmation:
            showFeedbackBottomSheet("Reset Email Sent", "Check your inbox for a password reset link.")
        }
    }
} 
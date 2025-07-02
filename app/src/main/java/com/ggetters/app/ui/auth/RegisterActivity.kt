package com.ggetters.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        registerButton.setOnClickListener {
            // TODO: Backend - Register new user
            // Endpoint: POST /api/auth/register
            // Request: { name: String, surname: String, email: String, password: String }
            // Response: { user: User, requiresVerification: Boolean }
            // Error: { message: String }
            // Notes: Validate email uniqueness, password complexity, and send verification email/OTP.
            // TODO: Backend - Log analytics event for registration
            // TODO: Backend - Trigger age verification after registration
            // On success:
            val intent = Intent(this, AgeVerificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
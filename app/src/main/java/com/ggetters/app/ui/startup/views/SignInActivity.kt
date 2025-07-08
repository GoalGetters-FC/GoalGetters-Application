package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_activity)

        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password_default)
        val loginButton = findViewById<Button>(R.id.bt_sign_in)
        val registerButton = findViewById<Button>(R.id.bt_sign_up)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        // TODO: Add Google SSO button logic
        // TODO: Add Help & FAQ bottom sheet logic

        loginButton.setOnClickListener {
            // TODO: Backend - Authenticate user
            // Endpoint: POST /api/auth/login
            // Request: { email: String, password: String }
            // Response: { token: String, user: User }
            // Error: { message: String }
            // Notes: Handle invalid credentials, network errors, and lockout after X attempts.
            // TODO: Backend - Log analytics event for login attempt
            // TODO: Backend - Integrate Google SSO (if Google login button is added)
            // Endpoint: POST /api/auth/google
            // Request: { idToken: String }
            // Response: { token: String, user: User }
            // TODO: Backend - Implement password reset trigger (if forgot password is used)
            // Endpoint: POST /api/auth/forgot-password
            // Request: { email: String }
            // Response: { success: Boolean }
            // Example error handling:
            // if (loginFailed) showFeedbackBottomSheet("Login Error", "Invalid email or password.")
            // On success:
            startActivity(Intent(this, WelcomeBackActivity::class.java))
            finishAffinity()
        }
        registerButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
} 
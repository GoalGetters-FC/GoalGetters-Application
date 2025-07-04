package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.dialogs.AgeVerificationBottomSheet

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val surnameEditText = findViewById<EditText>(R.id.surnameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
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
            AgeVerificationBottomSheet().show(supportFragmentManager, "AgeVerificationBottomSheet")
        }
        loginButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
} 
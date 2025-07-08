package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.startup.dialogs.AgeVerificationBottomSheet

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        
        val emailEditText = findViewById<EditText>(R.id.et_identity)
        val passwordEditText = findViewById<EditText>(R.id.et_password_default)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_password_confirm)
        val registerButton = findViewById<Button>(R.id.bt_sign_up)
        val loginButton = findViewById<TextView>(R.id.tv_sign_in)

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
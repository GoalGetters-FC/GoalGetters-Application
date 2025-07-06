package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.central.views.HomeActivity

class WelcomeBackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_back)
        // TODO: Backend - Log analytics event for successful login
        // On continue:
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
} 
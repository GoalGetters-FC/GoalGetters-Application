package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
// TODO: Backend - Fetch data for each tab (Notifications, Calendar, Players, Team Profile)
// TODO: Backend - Log analytics for tab navigation

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity acts as a bridge to MainActivity (the true dashboard with nav bar)
        // TODO: Backend - Fetch initial dashboard data if needed
        // TODO: Backend - Log analytics for dashboard entry
        // On success:
        startActivity(Intent(this, com.ggetters.app.ui.shared.MainActivity::class.java))
        finish()
    }
} 
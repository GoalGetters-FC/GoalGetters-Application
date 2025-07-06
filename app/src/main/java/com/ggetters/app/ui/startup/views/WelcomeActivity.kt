package com.ggetters.app.ui.startup.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.startup.dialogs.TeamOptionsBottomSheet

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        TeamOptionsBottomSheet().show(supportFragmentManager, "TeamOptionsBottomSheet")
    }
} 
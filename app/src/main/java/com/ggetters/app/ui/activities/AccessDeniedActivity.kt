package com.ggetters.app.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class AccessDeniedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_denied)
        // TODO: Backend - Log analytics event for access denied
    }
} 
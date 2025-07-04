package com.ggetters.app.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class JoinTeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_team)

        val teamCodeEditText = findViewById<EditText>(R.id.teamCodeEditText)
        val joinButton = findViewById<Button>(R.id.joinButton)

        joinButton.setOnClickListener {
            // TODO: Backend - Join team with code
        }
    }
} 
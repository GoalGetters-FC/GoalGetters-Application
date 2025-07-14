package com.ggetters.app.ui.startup.views

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R

class CreateTeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        val teamNameEditText = findViewById<EditText>(R.id.teamNameEditText)
        val createButton = findViewById<Button>(R.id.createButton)

        createButton.setOnClickListener {
            // TODO: Backend - Create team with name
        }
    }
} 
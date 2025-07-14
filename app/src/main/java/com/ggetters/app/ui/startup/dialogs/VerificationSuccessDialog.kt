package com.ggetters.app.ui.startup.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.ggetters.app.R

class VerificationSuccessDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_verification_success)
        val continueButton = findViewById<Button>(R.id.continueButton)
        // TODO: Backend - Log analytics event for verification success
        continueButton.setOnClickListener {
            // TODO: Navigate to Welcome screen
            dismiss()
        }
    }
} 
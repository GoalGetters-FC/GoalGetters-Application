package com.ggetters.app.core.extensions

import android.app.Activity
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ggetters.app.R
import android.widget.TextView
import android.widget.Button

fun Activity.showFeedbackBottomSheet(title: String, message: String) {
    val dialog = BottomSheetDialog(this)
    val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_feedback, null)
    view.findViewById<TextView>(R.id.bottomSheetTitle).text = title
    view.findViewById<TextView>(R.id.bottomSheetMessage).text = message
    view.findViewById<Button>(R.id.bottomSheetCloseButton).setOnClickListener {
        dialog.dismiss()
    }
    dialog.setContentView(view)
    dialog.show()
} 
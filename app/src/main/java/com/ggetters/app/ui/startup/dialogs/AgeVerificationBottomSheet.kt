package com.ggetters.app.ui.startup.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ggetters.app.R
import com.ggetters.app.ui.startup.views.OnboardingActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AgeVerificationBottomSheet : BottomSheetDialogFragment() {
    
    var onAgeVerified: ((Boolean) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_age_verification, container, false)
        val yesButton = view.findViewById<Button>(R.id.yesButton)
        val noButton = view.findViewById<Button>(R.id.noButton)

        yesButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (over 18)
            onAgeVerified?.invoke(true)
            dismiss()
        }
        noButton.setOnClickListener {
            // TODO: Backend - Log analytics event for age check (under 18)
            onAgeVerified?.invoke(false)
            AgeNoticeBottomSheet().show(parentFragmentManager, "AgeNoticeBottomSheet")
            dismiss()
        }
        return view
    }
} 
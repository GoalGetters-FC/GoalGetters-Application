package com.ggetters.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import com.ggetters.app.ui.activities.VerificationActivity

class AgeNoticeBottomSheet : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_age_notice, container, false)
        val consentButton = view.findViewById<Button>(R.id.consentButton)
        val exitButton = view.findViewById<Button>(R.id.exitButton)
        val learnMore = view.findViewById<TextView>(R.id.learnMoreText)

        consentButton.setOnClickListener {
            // TODO: Backend - Log consent event and handle underage consent logic
            startActivity(Intent(requireContext(), VerificationActivity::class.java))
            dismiss()
        }
        exitButton.setOnClickListener {
            // TODO: Backend - Log exit event and handle app exit for underage users
            requireActivity().finish()
        }
        learnMore.setOnClickListener {
            // TODO: Show more info (could open another bottom sheet or external link)
        }
        return view
    }
} 
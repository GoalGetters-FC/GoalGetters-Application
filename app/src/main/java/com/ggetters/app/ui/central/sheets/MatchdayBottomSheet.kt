package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ggetters.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement real-time match event recording
// TODO: Backend - Add event validation and permissions
// TODO: Backend - Implement event analytics and insights
// TODO: Backend - Add event sharing and social features

@AndroidEntryPoint
class MatchdayBottomSheet : BottomSheetDialogFragment() {

    private var matchId: String = ""
    
    companion object {
        fun newInstance(matchId: String): MatchdayBottomSheet {
            return MatchdayBottomSheet().apply {
                this.matchId = matchId
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_matchday, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupQuickActions()
    }

    private fun setupQuickActions() {
        // Goal actions
        view?.findViewById<MaterialButton>(R.id.btnGoalHome)?.setOnClickListener {
            recordEvent("Goal - Home Team")
        }
        
        view?.findViewById<MaterialButton>(R.id.btnGoalAway)?.setOnClickListener {
            recordEvent("Goal - Away Team")
        }
        
        // Card actions
        view?.findViewById<MaterialButton>(R.id.btnYellowCard)?.setOnClickListener {
            recordEvent("Yellow Card")
        }
        
        view?.findViewById<MaterialButton>(R.id.btnRedCard)?.setOnClickListener {
            recordEvent("Red Card")
        }
        
        // Substitution
        view?.findViewById<MaterialButton>(R.id.btnSubstitution)?.setOnClickListener {
            recordEvent("Substitution")
        }
        
        // Other actions
        view?.findViewById<MaterialButton>(R.id.btnFreeKick)?.setOnClickListener {
            recordEvent("Free Kick")
        }
        
        view?.findViewById<MaterialButton>(R.id.btnCorner)?.setOnClickListener {
            recordEvent("Corner Kick")
        }
        
        view?.findViewById<MaterialButton>(R.id.btnOffside)?.setOnClickListener {
            recordEvent("Offside")
        }
    }

    private fun recordEvent(eventType: String) {
        // TODO: Backend - Record actual match event
        Toast.makeText(context, "$eventType recorded", Toast.LENGTH_SHORT).show()
        dismiss()
    }
}


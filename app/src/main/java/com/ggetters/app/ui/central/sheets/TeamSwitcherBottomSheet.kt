package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.UserAccount

// TODO: Backend - Implement team switching with proper authentication
// TODO: Backend - Add team switching analytics and tracking
// TODO: Backend - Implement team switching notifications and confirmations
// TODO: Backend - Add team switching validation and permissions
// TODO: Backend - Implement team switching data synchronization

class TeamSwitcherBottomSheet : BottomSheetDialogFragment() {

    private var onTeamSelected: ((UserAccount) -> Unit)? = null
    private var onManageTeams: (() -> Unit)? = null

    companion object {
        const val TAG = "TeamSwitcherBottomSheet"

        fun newInstance(
            onTeamSelected: (UserAccount) -> Unit,
            onManageTeams: () -> Unit
        ): TeamSwitcherBottomSheet {
            return TeamSwitcherBottomSheet().apply {
                this.onTeamSelected = onTeamSelected
                this.onManageTeams = onManageTeams
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_team_switcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        // TODO: Backend - Load user's teams from backend
        // TODO: Backend - Implement team data caching for offline access
        // TODO: Backend - Add team data synchronization across devices
        // TODO: Backend - Implement team data validation and integrity checks
        // TODO: Backend - Add team data analytics and usage tracking
    }

    private fun setupClickListeners() {
        // Close button
        view?.findViewById<View>(R.id.closeButton)?.setOnClickListener {
            dismiss()
        }

        // Switch team button
        view?.findViewById<MaterialButton>(R.id.switchTeamButton)?.setOnClickListener {
            // TODO: Backend - Implement team switching logic
            Snackbar.make(requireView(), "Team switching functionality coming soon!", Snackbar.LENGTH_SHORT).show()
            dismiss()
        }

        view?.findViewById<MaterialButton>(R.id.manageTeamsButton)?.setOnClickListener {
            handleManageTeams()
        }

        // Setup radio button listeners
        view?.findViewById<RadioButton>(R.id.currentTeamRadio)?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view?.findViewById<RadioButton>(R.id.otherTeamRadio)?.isChecked = false
            }
        }

        view?.findViewById<RadioButton>(R.id.otherTeamRadio)?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view?.findViewById<RadioButton>(R.id.currentTeamRadio)?.isChecked = false
            }
        }
    }

    private fun handleTeamSwitch() {
        // TODO: Backend - Implement team switching with proper validation
        // TODO: Backend - Add team switching analytics and tracking
        // TODO: Backend - Implement team switching notifications and confirmations
        // TODO: Backend - Add team switching audit logging
        // TODO: Backend - Implement team switching data synchronization

        val selectedTeam = when {
            view?.findViewById<RadioButton>(R.id.currentTeamRadio)?.isChecked == true -> {
                // Current team (U15a Football)
                UserAccount(
                    "1",
                    "Matthew Pieterse",
                    "matthew@example.com",
                    null,
                    "U15a Football",
                    "Coach",
                    true
                )
            }
            view?.findViewById<RadioButton>(R.id.otherTeamRadio)?.isChecked == true -> {
                // Other team (Seniors League)
                UserAccount(
                    "2",
                    "Matthew Pieterse",
                    "matthew@example.com",
                    null,
                    "Seniors League",
                    "F-P",
                    false
                )
            }
            else -> null
        }

        selectedTeam?.let { team ->
            onTeamSelected?.invoke(team)
            Snackbar.make(requireView(), "Switched to ${team.teamName}", Snackbar.LENGTH_SHORT).show()
            dismiss()
        } ?: run {
            Snackbar.make(requireView(), "Please select a team", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleManageTeams() {
        // TODO: Backend - Navigate to team management screen
        // TODO: Backend - Add team management analytics and tracking
        // TODO: Backend - Implement team management permissions and validation
        // TODO: Backend - Add team management audit logging
        // TODO: Backend - Implement team management data synchronization

        onManageTeams?.invoke()
        dismiss()
    }
} 
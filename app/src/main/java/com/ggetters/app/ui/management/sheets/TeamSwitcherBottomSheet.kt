package com.ggetters.app.ui.management.sheets

import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TeamSwitcherBottomSheet : BottomSheetDialogFragment() {

    private var onTeamSelected: ((UserAccount) -> Unit)? = null
    private var onManageTeams: (() -> Unit)? = null
    private var onSetDefaultTeam: ((String) -> Unit)? = null

    // Share the same VM as TeamViewerActivity (or the host screen)
    private val model: TeamViewerViewModel by activityViewModels()

    private var selectedTeamId: String? = null

    companion object {
        const val TAG = "TeamSwitcherBottomSheet"

        fun newInstance(
            onTeamSelected: (UserAccount) -> Unit,
            onManageTeams: () -> Unit,
            onSetDefaultTeam: (String) -> Unit
        ): TeamSwitcherBottomSheet {
            return TeamSwitcherBottomSheet().apply {
                this.onTeamSelected = onTeamSelected
                this.onManageTeams = onManageTeams
                this.onSetDefaultTeam = onSetDefaultTeam
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
        setEnterTransition(Fade())
        setExitTransition(Fade())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_team_switcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeTeamsAndActive(view)
    }

    // Build the radio list from DB + pre-check current active team
    private fun observeTeamsAndActive(root: View) {
        val group = root.findViewById<RadioGroup>(R.id.teamRadioGroup)
            ?: error("Layout must contain a RadioGroup with id @id/teamRadioGroup")

        val switchBtn = root.findViewById<com.google.android.material.button.MaterialButton>(R.id.switchTeamButton)

        viewLifecycleOwner.lifecycleScope.launch {
            model.teams.collectLatest { teams ->
                val activeId = teams.firstOrNull { it.isActive }?.id

                group.setOnCheckedChangeListener(null)
                group.removeAllViews()
                selectedTeamId = null

                if (teams.isEmpty()) {
                    // Empty state: disabled radio with a friendly label
                    val empty = com.google.android.material.radiobutton.MaterialRadioButton(requireContext()).apply {
                        id = View.generateViewId()
                        text = getString(R.string.no_active_team)
                        isEnabled = false
                    }
                    group.addView(empty)
                    switchBtn?.isEnabled = false
                } else {
                    // Build radios from real teams
                    teams.forEach { team ->
                        val rb = com.google.android.material.radiobutton.MaterialRadioButton(requireContext()).apply {
                            id = View.generateViewId()
                            text = team.name
                            tag = team.id
                            isChecked = team.id == activeId
                        }
                        group.addView(rb)
                    }

                    // default selection: active team if present, otherwise first team
                    val defaultId = activeId ?: teams.first().id
                    selectedTeamId = defaultId

                    // Check the matching radio
                    val radioToCheck = (0 until group.childCount)
                        .map { group.getChildAt(it) as? RadioButton }
                        .firstOrNull { it?.tag == defaultId }
                    radioToCheck?.let { group.check(it.id) }

                    switchBtn?.isEnabled = true
                }

                // Update selectedTeamId on user change
                group.setOnCheckedChangeListener { g, checkedId ->
                    selectedTeamId = g.findViewById<RadioButton>(checkedId)?.tag as? String
                    switchBtn?.isEnabled = selectedTeamId != null
                }
            }
        }
    }


    private fun setupClickListeners() {
        view?.findViewById<View>(R.id.closeButton)?.setOnClickListener { dismiss() }

        view?.findViewById<MaterialButton>(R.id.switchTeamButton)?.setOnClickListener {
            handleTeamSwitch()
        }

        view?.findViewById<MaterialButton>(R.id.manageTeamsButton)?.setOnClickListener {
            handleManageTeams()
        }

        // If your layout has a toggle to set default team, keep it wired
        view?.findViewById<Switch>(R.id.defaultTeamSwitch)?.setOnCheckedChangeListener { _, isChecked ->
            handleDefaultTeamToggle(isChecked)
        }
    }

    private fun handleTeamSwitch() {
        val id = selectedTeamId ?: run {
            Snackbar.make(requireView(), "Please select a team", Snackbar.LENGTH_SHORT).show()
            return
        }

        val team = model.teams.value.firstOrNull { it.id == id } ?: run {
            Snackbar.make(requireView(), "Team not found", Snackbar.LENGTH_SHORT).show()
            return
        }

        model.switchTo(team)
        Toast.makeText(requireContext(), "Switched to ${team.name}", Toast.LENGTH_SHORT).show()

        onTeamSelected?.invoke(
            UserAccount(
                id = "N/A",
                name = "N/A",
                email = "N/A",
                avatar = null,      // <- correct param name
                teamName = team.name,
                role = "N/A",
                isActive = true
            )
        )

        dismiss()
    }


    private fun handleDefaultTeamToggle(isDefault: Boolean) {
        val id = selectedTeamId ?: model.teams.value.firstOrNull { it.isActive }?.id ?: return
        onSetDefaultTeam?.invoke(id)
        if (isDefault) {
            Snackbar.make(requireView(), "Default team updated", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleManageTeams() {
        onManageTeams?.invoke()
        dismiss()
    }
}

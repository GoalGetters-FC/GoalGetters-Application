package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggetters.app.R
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.databinding.BottomSheetTeamProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamProfileBottomSheet : BottomSheetDialogFragment() {
    
    companion object {
        private const val ARG_TEAM_ID = "team_id"
        
        fun newInstance(teamId: String): TeamProfileBottomSheet {
            return TeamProfileBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEAM_ID, teamId)
                }
            }
        }
    }
    
    private var _binding: BottomSheetTeamProfileBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTeamProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val teamId = arguments?.getString(ARG_TEAM_ID)
        if (teamId != null) {
            // For now, just show placeholder team info
            displayTeamInfo(getPlaceholderTeam())
        }
        
        binding.btnClose.setOnClickListener { dismiss() }
    }
    
    private fun getPlaceholderTeam(): Team {
        return Team(
            id = "placeholder",
            name = "Current Team",
            code = "ABC123",
            composition = TeamComposition.UNISEX_MALE,
            denomination = TeamDenomination.OPEN
        )
    }
    
    private fun displayTeamInfo(team: Team) {
        binding.apply {
            tvTeamName.text = team.name
            tvTeamCode.text = "Code: ${team.code}"
            tvTeamComposition.text = "Composition: ${team.composition.name}"
            tvTeamDenomination.text = "Type: ${team.denomination.name}"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


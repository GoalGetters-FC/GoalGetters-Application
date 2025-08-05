package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ggetters.app.R
import com.ggetters.app.ui.central.viewmodels.HomeProfileViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamProfileFragment : Fragment() {

    private val activeModel: HomeProfileViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var teamDivisionText: TextView
    private lateinit var teamAgeGroupText: TextView
    private lateinit var teamContactText: TextView
    private lateinit var rosterButton: MaterialButton

    // TODO: Backend - Get user role from backend/UserRepository
    // Simulate user role for demo ("coach", "assistant", "player", "guardian")
    private val userRole = "coach"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners()
        loadTeamData()
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        // Note: These IDs don't exist in the current layout, but we'll keep them for future use
        // teamDivisionText = view.findViewById(R.id.teamDivisionText)
        // teamAgeGroupText = view.findViewById(R.id.teamAgeGroupText)
        // teamContactText = view.findViewById(R.id.teamContactText)
        // rosterButton = view.findViewById(R.id.rosterButton)
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // rosterButton.setOnClickListener {
        //     navigateToPlayerList()
        // }
    }

    private fun navigateToPlayerList() {
        // TODO: Backend - Navigate to player list with team data
        val playerListFragment = PlayerListFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, playerListFragment)
            .addToBackStack("team_profile_to_player_list")
            .commit()
    }

    private fun loadTeamData() {
        // TODO: Backend - Fetch team data from backend
        // val team = teamRepo.getById(currentTeamId)
        
        // Sample data for demo
        // teamDivisionText.text = "Division: Premier League"
        // teamAgeGroupText.text = "Age Group: U16"
        // teamContactText.text = "Contact: coach@email.com"
        
        Snackbar.make(requireView(), "Team profile loaded", Snackbar.LENGTH_SHORT).show()
    }
} 
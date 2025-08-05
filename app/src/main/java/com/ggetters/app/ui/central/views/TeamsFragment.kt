package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.TeamAdapter
import com.ggetters.app.ui.central.models.UserAccount
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamsFragment : Fragment() {

    private lateinit var teamsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var linkTeamCodeButton: MaterialButton
    private lateinit var createTeamButton: MaterialButton
    private lateinit var teamAdapter: TeamAdapter

    private var userTeams: List<UserAccount> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_teams, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadTeams()
    }

    private fun setupViews(view: View) {
        teamsRecyclerView = view.findViewById(R.id.teamsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        linkTeamCodeButton = view.findViewById(R.id.linkTeamCodeButton)
        createTeamButton = view.findViewById(R.id.createTeamButton)
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamAdapter(
            onTeamClick = { team ->
                navigateToTeamProfile(team)
            },
            onSetDefaultClick = { team ->
                setDefaultTeam(team)
            }
        )
        teamsRecyclerView.layoutManager = LinearLayoutManager(context)
        teamsRecyclerView.adapter = teamAdapter
    }

    private fun setupClickListeners() {
        linkTeamCodeButton.setOnClickListener {
            showLinkTeamCodeDialog()
        }

        createTeamButton.setOnClickListener {
            navigateToCreateTeam()
        }
    }

    private fun loadTeams() {
        // TODO: Backend - Fetch user's teams from backend
        // userTeams = teamRepo.getUserTeams(currentUserId)
        
        // Sample data for demo
        userTeams = listOf(
            UserAccount(
                "1",
                "Matthew Pieterse",
                "matthew@example.com",
                null,
                "U15a Football",
                "Player",
                true // isActive/default
            ),
            UserAccount(
                "2",
                "Matthew Pieterse",
                "matthew@example.com",
                null,
                "City FC",
                "Coach",
                false
            ),
            UserAccount(
                "3",
                "Matthew Pieterse",
                "matthew@example.com",
                null,
                "Local League",
                "Player",
                false
            )
        )

        teamAdapter.updateTeams(userTeams)
        updateEmptyState(userTeams.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        teamsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun navigateToTeamProfile(team: UserAccount) {
        // TODO: Backend - Navigate to team profile with team data
        val teamProfileFragment = TeamProfileFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, teamProfileFragment)
            .addToBackStack("teams_to_team_profile")
            .commit()
    }

    private fun setDefaultTeam(team: UserAccount) {
        // TODO: Backend - Call backend to set default team
        // teamRepo.setDefaultTeam(team.id, currentUserId)
        
        // Update local state
        userTeams = userTeams.map { 
            it.copy(isActive = it.id == team.id)
        }
        teamAdapter.updateTeams(userTeams)
        
        Snackbar.make(requireView(), "${team.teamName} set as default", Snackbar.LENGTH_SHORT).show()
    }

    private fun showLinkTeamCodeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_invite_code, null)
        val codeEditText = dialogView.findViewById<EditText>(R.id.codeEditText)

        AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Join Team")
            .setMessage("Enter the team invite code to join")
            .setView(dialogView)
            .setPositiveButton("Join") { _, _ ->
                val code = codeEditText.text.toString().trim()
                if (code.isNotEmpty()) {
                    joinTeamWithCode(code)
                } else {
                    Snackbar.make(requireView(), "Please enter a valid code", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun joinTeamWithCode(code: String) {
        // TODO: Backend - Call backend to join team with code
        // teamRepo.joinTeamWithCode(code, currentUserId)
        
        Snackbar.make(requireView(), "Joining team...", Snackbar.LENGTH_SHORT).show()
        
        // Refresh teams list
        loadTeams()
    }

    private fun navigateToCreateTeam() {
        // TODO: Backend - Navigate to team creation screen
        Snackbar.make(requireView(), "Create team functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
} 
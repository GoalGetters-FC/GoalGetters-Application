package com.ggetters.app.ui.central.views

import android.app.AlertDialog
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar

/**
 * Lightweight model just for this demo screen.
 * Replace with your real Team entity + repository later.
 */
private data class ViewTeam(
    val name: String,
    val founded: String,
    val homeGround: String,
    val coach: String,
    val captain: String
)

class TeamProfileFragment : Fragment() {

    private val activeModel: HomeProfileViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var teamBannerImage: ImageView
    private lateinit var teamLogo: ImageView
    private lateinit var teamNameText: TextView
    private lateinit var teamCodeText: TextView
    private lateinit var ageGroupText: TextView
    private lateinit var genderText: TextView
    private lateinit var locationText: TextView
    private lateinit var seasonText: TextView
    private lateinit var contactText: TextView
    private lateinit var roleChips: ChipGroup
    private lateinit var btnViewPlayers: MaterialButton
    private lateinit var btnViewSchedule: MaterialButton
    private lateinit var btnLeaveTeam: MaterialButton
    private lateinit var btnInvite: MaterialButton
    private lateinit var btnEditTeam: MaterialButton
    private lateinit var btnManageRoles: MaterialButton
    private lateinit var btnDeleteTeam: MaterialButton

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
        loadTeamProfile()
        setupRoleVisibility()
        setupActions()
    }

    private fun setupViews(view: View) {
        teamBannerImage = view.findViewById(R.id.teamBannerImage)
        teamLogo = view.findViewById(R.id.teamLogo)
        teamNameText = view.findViewById(R.id.teamNameText)
        teamCodeText = view.findViewById(R.id.teamCodeText)
        ageGroupText = view.findViewById(R.id.ageGroupText)
        genderText = view.findViewById(R.id.genderText)
        locationText = view.findViewById(R.id.locationText)
        seasonText = view.findViewById(R.id.seasonText)
        contactText = view.findViewById(R.id.contactText)
        roleChips = view.findViewById(R.id.roleChips)
        btnViewPlayers = view.findViewById(R.id.btnViewPlayers)
        btnViewSchedule = view.findViewById(R.id.btnViewSchedule)
        btnLeaveTeam = view.findViewById(R.id.btnLeaveTeam)
        btnInvite = view.findViewById(R.id.btnInvite)
        btnEditTeam = view.findViewById(R.id.btnEditTeam)
        btnManageRoles = view.findViewById(R.id.btnManageRoles)
        btnDeleteTeam = view.findViewById(R.id.btnDeleteTeam)
    }

    private fun loadTeamProfile() {
        // TODO: Backend - Fetch team data from backend (replace sample data)
        // val team = teamRepo.getById(currentTeamId)
        // val stats = teamStatsRepo.getStatsFor(team.id)
        teamBannerImage.setImageResource(R.drawable.team_banner_default)
        teamLogo.setImageResource(R.drawable.ic_unicons_soccer_24)
        teamNameText.text = "Greenfield U16 Lions" // TODO: team.name
        teamCodeText.text = "U16-LIONS" // TODO: team.shortCode
        ageGroupText.text = "Age Group: U16" // TODO: team.ageGroup
        genderText.text = "Gender: Co-ed" // TODO: team.gender
        locationText.text = "Location: Main Stadium" // TODO: team.location
        seasonText.text = "Season: 2025/2026" // TODO: team.season
        contactText.text = "Contact: coach@email.com" // TODO: team.contact
        
        // TODO: Backend - Set role chips counts from backend
        val chipCoach = roleChips.findViewById<Chip>(R.id.chipCoach)
        chipCoach.text = "Coach: 1" // TODO: team.coachCount
        val chipAssistants = roleChips.findViewById<Chip>(R.id.chipAssistants)
        chipAssistants.text = "Assistants: 2" // TODO: team.assistantCount
        val chipPlayers = roleChips.findViewById<Chip>(R.id.chipPlayers)
        chipPlayers.text = "Players: 15" // TODO: team.playerCount
    }

    private fun setupRoleVisibility() {
        // Role-based visibility according to specifications
        when (userRole) {
            "coach" -> {
                // Coach: Full edit, invite, delete, assign roles
            btnInvite.visibility = View.VISIBLE
            btnEditTeam.visibility = View.VISIBLE
            btnManageRoles.visibility = View.VISIBLE
            btnDeleteTeam.visibility = View.VISIBLE
                btnLeaveTeam.visibility = View.GONE
            }
            "assistant" -> {
                // Assistant: Limited edit, manage schedule, assist lineup
                btnInvite.visibility = View.VISIBLE
                btnEditTeam.visibility = View.VISIBLE
                btnManageRoles.visibility = View.VISIBLE
                btnDeleteTeam.visibility = View.GONE
                btnLeaveTeam.visibility = View.GONE
            }
            "player" -> {
                // Player: View only; RSVP to events
                btnInvite.visibility = View.GONE
                btnEditTeam.visibility = View.GONE
                btnManageRoles.visibility = View.GONE
                btnDeleteTeam.visibility = View.GONE
                btnLeaveTeam.visibility = View.VISIBLE
            }
            "guardian" -> {
                // Guardian: View child's team, calendar, stats
                btnInvite.visibility = View.GONE
                btnEditTeam.visibility = View.GONE
                btnManageRoles.visibility = View.GONE
                btnDeleteTeam.visibility = View.GONE
                btnLeaveTeam.visibility = View.VISIBLE
            }
            else -> {
                // Default: Hide all admin actions
            btnInvite.visibility = View.GONE
            btnEditTeam.visibility = View.GONE
            btnManageRoles.visibility = View.GONE
            btnDeleteTeam.visibility = View.GONE
                btnLeaveTeam.visibility = View.GONE
            }
        }
    }

    private fun setupActions() {
        btnViewPlayers.setOnClickListener {
            // Navigate to Players screen
            navigateToPlayersScreen()
        }
        
        btnViewSchedule.setOnClickListener {
            // Navigate to Schedule/Calendar screen
            navigateToCalendarScreen()
        }
        
        btnLeaveTeam.setOnClickListener {
            showLeaveTeamConfirmation()
        }
        
        btnInvite.setOnClickListener {
            showInviteDialog()
        }
        
        btnEditTeam.setOnClickListener {
            showEditTeamDialog()
        }
        
        btnManageRoles.setOnClickListener {
            showManageRolesDialog()
        }
        
        btnDeleteTeam.setOnClickListener {
            showDeleteTeamConfirmation()
        }
    }

    private fun navigateToPlayersScreen() {
        // TODO: Backend - Log navigation analytics
        val playersFragment = HomePlayersFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, playersFragment)
            .addToBackStack("team_profile_to_players")
            .commit()
    }

    private fun navigateToCalendarScreen() {
        // TODO: Backend - Log navigation analytics
        val calendarFragment = HomeCalendarFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, calendarFragment)
            .addToBackStack("team_profile_to_calendar")
            .commit()
    }

    private fun showLeaveTeamConfirmation() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Leave Team")
            .setMessage("Are you sure you want to leave this team? You can rejoin later with an invite code.")
            .setPositiveButton("Leave Team") { _, _ ->
                // TODO: Backend - Call backend to leave team
                // teamRepo.leaveTeam(currentTeamId, currentUserId)
                Snackbar.make(requireView(), "Left team successfully", Snackbar.LENGTH_SHORT).show()
                // TODO: Navigate to team selection or onboarding
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.error, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        
        dialog.show()
    }

    private fun showInviteDialog() {
        // TODO: Backend - Fetch invite code from backend
        val inviteCode = "U16-LIONS-2025" // TODO: team.inviteCode
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Invite New Members")
            .setMessage("Share this invite code with new members:\n\n$inviteCode")
            .setPositiveButton("Copy Code") { _, _ ->
                // TODO: Copy to clipboard
                Snackbar.make(requireView(), "Invite code copied to clipboard", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Share") { _, _ ->
                // TODO: Share via intent (WhatsApp, Email, etc.)
                Snackbar.make(requireView(), "Share functionality coming soon", Snackbar.LENGTH_SHORT).show()
            }
            .setNeutralButton("Close", null)
            .create()
        
        dialog.setOnShowListener {
            // Apply custom colors to buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.primary, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.text_secondary, null))
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        
        dialog.show()
    }

    private fun showEditTeamDialog() {
        // TODO: Backend - Show edit team info dialog/screen
        Snackbar.make(requireView(), "Edit team functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showManageRolesDialog() {
        // TODO: Backend - Show manage roles dialog/screen
        Snackbar.make(requireView(), "Role management functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showDeleteTeamConfirmation() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Delete Team")
            .setMessage("Are you sure you want to delete this team? This action cannot be undone and will remove all team data.")
            .setPositiveButton("Delete Team") { _, _ ->
                // TODO: Backend - Call backend to delete team
                // teamRepo.deleteTeam(currentTeamId)
                Snackbar.make(requireView(), "Team deleted successfully", Snackbar.LENGTH_SHORT).show()
                // TODO: Navigate to team selection or onboarding
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.error, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        
        dialog.show()
    }
}

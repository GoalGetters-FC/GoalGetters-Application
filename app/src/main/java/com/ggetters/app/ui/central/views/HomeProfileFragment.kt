package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.TeamStats
import android.widget.ImageView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.ggetters.app.ui.central.views.HomePlayersFragment

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
    private lateinit var adminActions: View
    private lateinit var sharedActions: View
    private lateinit var btnViewPlayers: MaterialButton
    private lateinit var btnViewSchedule: MaterialButton
    private lateinit var btnLeaveTeam: MaterialButton
    private lateinit var btnInvite: MaterialButton
    private lateinit var btnEditTeam: MaterialButton
    private lateinit var btnManageRoles: MaterialButton
    private lateinit var btnDeleteTeam: MaterialButton

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
        adminActions = view.findViewById(R.id.adminActions)
        sharedActions = view.findViewById(R.id.sharedActions)
        btnViewPlayers = view.findViewById(R.id.btnViewPlayers)
        btnViewSchedule = view.findViewById(R.id.btnViewSchedule)
        btnLeaveTeam = view.findViewById(R.id.btnLeaveTeam)
        btnInvite = view.findViewById(R.id.btnInvite)
        btnEditTeam = view.findViewById(R.id.btnEditTeam)
        btnManageRoles = view.findViewById(R.id.btnManageRoles)
        btnDeleteTeam = view.findViewById(R.id.btnDeleteTeam)
    }

    private fun loadTeamProfile() {
        // TODO: Fetch team data from backend (replace sample data)
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
        // TODO: Set role chips counts from backend
        val chipCoach = roleChips.findViewById<Chip>(R.id.chipCoach)
        chipCoach.text = "Coach: 1" // TODO: team.coachCount
        val chipAssistants = roleChips.findViewById<Chip>(R.id.chipAssistants)
        chipAssistants.text = "Assistants: 2" // TODO: team.assistantCount
        val chipPlayers = roleChips.findViewById<Chip>(R.id.chipPlayers)
        chipPlayers.text = "Players: 15" // TODO: team.playerCount
    }

    private fun setupRoleVisibility() {
        // Only show admin actions for coach/assistant
        if (userRole == "coach" || userRole == "assistant") {
            adminActions.visibility = View.VISIBLE
        } else {
            adminActions.visibility = View.GONE
        }
        // Only show leave team for player/guardian
        btnLeaveTeam.visibility = if (userRole == "player" || userRole == "guardian") View.VISIBLE else View.GONE
    }

    private fun setupActions() {
        btnViewPlayers.setOnClickListener {
            // TODO: Navigate to Players screen
        }
        btnViewSchedule.setOnClickListener {
            // TODO: Navigate to Schedule/Calendar screen
        }
        btnLeaveTeam.setOnClickListener {
            // TODO: Show leave team confirmation and call backend to leave team
        }
        btnInvite.setOnClickListener {
            // TODO: Show invite code/QR/share dialog (fetch invite code from backend)
        }
        btnEditTeam.setOnClickListener {
            // TODO: Show edit team info dialog/screen (update backend)
        }
        btnManageRoles.setOnClickListener {
            // TODO: Show manage roles dialog/screen (update backend)
        }
        btnDeleteTeam.setOnClickListener {
            // TODO: Show delete team confirmation and call backend to delete team
        }
    }
}

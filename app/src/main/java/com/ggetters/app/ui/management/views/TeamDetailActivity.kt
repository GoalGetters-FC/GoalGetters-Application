package com.ggetters.app.ui.management.views

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ActivityTeamDetailBinding
import com.ggetters.app.ui.central.dialogs.EditTeamDialog
import com.ggetters.app.ui.management.viewmodels.TeamDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamDetailActivity : AppCompatActivity(), EditTeamDialog.EditTeamDialogListener {


    private lateinit var binds: ActivityTeamDetailBinding
    private val model: TeamDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(TAG, "Created a new instance of the activity")

        setupBindings()
        setupLayoutUi()
        setupViews()
        loadTeamData()

        observe()
    }

    private fun setupBindings() {
        binds = ActivityTeamDetailBinding.inflate(layoutInflater)
        setContentView(binds.root)
    }

    private fun setupLayoutUi() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binds.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViews() {
        // Setup toolbar
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "Team Details"

        // Setup click listeners
        binds.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup menu click listeners
        binds.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_team -> {
                    navigateToEditTeam()
                    true
                }
                R.id.action_share_team -> {
                    // TODO: Share team information
                    Clogger.d(TAG, "Share team clicked")
                    true
                }
                R.id.action_team_settings -> {
                    // TODO: Navigate to team settings
                    Clogger.d(TAG, "Team settings clicked")
                    true
                }
                R.id.action_delete_team -> {
                    // TODO: Show delete confirmation dialog
                    Clogger.d(TAG, "Delete team clicked")
                    true
                }
                else -> false
            }
        }

        // Setup team info sections
        setupTeamInfoSection()
        setupTeamStatsSection()
        setupTeamActionsSection()
    }

    private fun setupTeamInfoSection() {
        // TODO: Backend - Load team information from backend
        // TODO: Backend - Implement team info editing functionality
        // TODO: Backend - Add team info validation and permissions
        // TODO: Backend - Implement team info synchronization
        // TODO: Backend - Add team info analytics and tracking

        val teamName = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "U15a Football"
        binds.teamNameText.text = teamName
        binds.teamDescriptionText.text = "Under 15 football team"
        binds.teamCompositionText.text = "Unisex (Male)"
        binds.teamAgeGroupText.text = "All U15"
    }

    private fun setupTeamStatsSection() {
        // TODO: Backend - Load team statistics from backend
        // TODO: Backend - Implement team stats calculation and caching
        // TODO: Backend - Add team stats filtering and date ranges
        // TODO: Backend - Implement team stats export and sharing
        // TODO: Backend - Add team stats analytics and tracking

        binds.memberCountText.text = "15"
        binds.coachCountText.text = "2"
        binds.playerCountText.text = "12"
        binds.guardianCountText.text = "1"
    }

    private fun setupTeamActionsSection() {
        // TODO: Backend - Implement team action permissions and validation
        // TODO: Backend - Add team action analytics and tracking
        // TODO: Backend - Implement team action notifications and confirmations
        // TODO: Backend - Add team action audit logging
        // TODO: Backend - Implement team action data synchronization

        binds.viewPlayersButton.setOnClickListener {
            // TODO: Navigate to team players list
            Clogger.d(TAG, "View players clicked")
        }

        binds.editTeamButton.setOnClickListener {
            navigateToEditTeam()
        }

        binds.viewStatsButton.setOnClickListener {
            // TODO: Navigate to team statistics
            Clogger.d(TAG, "View stats clicked")
        }

        binds.inviteMembersButton.setOnClickListener {
            // TODO: Show invite members dialog
            Clogger.d(TAG, "Invite members clicked")
        }
    }

    private fun loadTeamData() {
        val teamId = intent.getStringExtra(EXTRA_TEAM_ID)
        if (teamId != null) {
            // TODO: Backend - Load team data from backend using teamId
            // TODO: Backend - Implement team data caching for offline access
            // TODO: Backend - Add team data synchronization across devices
            // TODO: Backend - Implement team data validation and integrity checks
            // TODO: Backend - Add team data analytics and usage tracking

            Clogger.d(TAG, "Loading team data for team ID: $teamId")
        }
    }

    private fun observe() {
        // TODO: Observe view-model data changes
        // TODO: Backend - Implement real-time team data updates
        // TODO: Backend - Add team data change notifications
        // TODO: Backend - Implement team data conflict resolution
        // TODO: Backend - Add team data analytics and tracking
    }

    private fun navigateToEditTeam() {
        val editDialog = EditTeamDialog.newInstance(
            teamId = intent.getStringExtra(EXTRA_TEAM_ID),
            teamName = binds.teamNameText.text.toString(),
            teamCode = "TES", // TODO: Get from backend
            teamAlias = "Test", // TODO: Get from backend
            teamDescription = binds.teamDescriptionText.text.toString(),
            teamComposition = "UNISEX_MALE", // TODO: Get from backend
            teamDenomination = "OPEN", // TODO: Get from backend
            yearFormed = "2025", // TODO: Get from backend
            contactCell = "", // TODO: Get from backend
            contactMail = "", // TODO: Get from backend
            clubAddress = "" // TODO: Get from backend
        )
        editDialog.show(supportFragmentManager, EditTeamDialog.TAG)
    }

    override fun onTeamUpdated(updatedTeam: Team) {
        // Refresh team data after successful edit
        binds.teamNameText.text = updatedTeam.name
        binds.teamDescriptionText.text = updatedTeam.description ?: "No description"
        // TODO: Update other fields when backend integration is complete
        Clogger.d(TAG, "Team updated: ${updatedTeam.name}")
    }

    companion object {
        private const val TAG = "TeamDetailActivity"
        const val EXTRA_TEAM_ID = "team_id"
        const val EXTRA_TEAM_NAME = "team_name"
    }
} 
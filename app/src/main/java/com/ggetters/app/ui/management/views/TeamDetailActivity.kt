package com.ggetters.app.ui.management.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ActivityTeamDetailBinding
import com.ggetters.app.ui.central.dialogs.EditTeamDialog
import com.ggetters.app.ui.management.viewmodels.TeamDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        // Style toolbar similar to TeamViewerActivity (dark header aesthetics)
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binds.toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161620"))
        binds.toolbar.setTitleTextColor(android.graphics.Color.WHITE)
        // Ensure status bar icons are light on dark background
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }

    private fun setupViews() {
        // Setup toolbar
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "Team Details"

        // Setup click listeners
        binds.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
//        // TODO: Backend - Load team information from backend
//        // TODO: Backend - Implement team info editing functionality
//        // TODO: Backend - Add team info validation and permissions
//        // TODO: Backend - Implement team info synchronization
//        // TODO: Backend - Add team info analytics and tracking
//
//        val teamName = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "U15a Football"
//        binds.teamNameText.text = teamName
//        binds.teamDescriptionText.text = "Under 15 football team"
//        binds.teamCompositionText.text = "Unisex (Male)"
//        binds.teamAgeGroupText.text = "All U15"
    }

    private fun setupTeamStatsSection() {
        // TODO: Backend - Load team statistics from backend
        // TODO: Backend - Implement team stats calculation and caching
        // TODO: Backend - Add team stats filtering and date ranges
        // TODO: Backend - Implement team stats export and sharing
        // TODO: Backend - Add team stats analytics and tracking

        // Real-time stats pending: hide placeholders to avoid seeded data
        binds.memberCountText.text = "—"
        binds.coachCountText.text = "—"
        binds.playerCountText.text = "—"
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

        // Removed View Statistics button

        binds.inviteMembersButton.setOnClickListener {
            showInviteMembersDialog()
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
            // Ensure persistent code exists
            model.ensureTeamCode(teamId)
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.team.collectLatest { team ->
                    if (team == null) {
                        supportActionBar?.title = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "Team Details"
                        return@collectLatest
                    }
                    supportActionBar?.title = team.name
                    binds.teamNameText.text = team.name
                    binds.teamDescriptionText.text = team.description ?: "—"
                    binds.teamCompositionText.text =
                        team.composition.name.replace('_',' ')
                            .lowercase().replaceFirstChar { it.uppercase() }
                    binds.teamAgeGroupText.text = team.denomination.name.replace('_',' ')
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.memberCount.collectLatest { count ->
                    binds.memberCountText.text = count.toString()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.coachCount.collectLatest { count ->
                    binds.coachCountText.text = count.toString()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.playerCount.collectLatest { count ->
                    binds.playerCountText.text = count.toString()
                }
            }
        }
    }



    private fun showInviteMembersDialog() {
        val t = model.team.value
        if (t == null) {
            Toast.makeText(this, "Team still loading…", Toast.LENGTH_SHORT).show()
            return
        }
        val code = (t.code ?: "").uppercase()

        val dialogView = layoutInflater.inflate(R.layout.dialog_invite_code, null)
        val codeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.codeEditText)
        codeInput.setText(code)

        val dlg = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Invite Members")
            .setMessage("Share this team code so members can join.")
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .setPositiveButton("Share") { _, _ ->
                val shareText = "Join our team on GoalGetters FC!\n\nTeam Code: ${codeInput.text?.toString()?.uppercase()}\n\nOpen the app and enter this code to join."
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Join our team")
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Share team code"))
            }
            .setNeutralButton("Copy") { _, _ ->
                val text = codeInput.text?.toString()?.uppercase() ?: ""
                val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Team Code", text))
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            }
            .create()
        dlg.show()
    }

    private fun generateTeamCodeLocal(): String {
        val chars = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray()
        val rnd = java.security.SecureRandom()
        val sb = StringBuilder()
        repeat(6) { sb.append(chars[rnd.nextInt(chars.size)]) }
        return sb.toString()
    }

    private fun navigateToEditTeam() {
        val t = model.team.value
        if (t == null) {
            Toast.makeText(this, "Team still loading…", Toast.LENGTH_SHORT).show()
            return
        }

        EditTeamDialog.newInstance(
            teamId          = t.id,
            teamName        = t.name,
            teamCode        = t.code ?: "",
            teamAlias       = t.alias ?: "",
            teamDescription = t.description ?: "",
            // dialog expects enum names (e.g., "UNISEX_MALE", "OPEN")
            teamComposition = t.composition.name,
            teamDenomination= t.denomination.name,
            yearFormed      = t.yearFormed?.toString() ?: "",
            contactCell     = t.contactCell ?: "",
            contactMail     = t.contactMail ?: "",
            clubAddress     = t.clubAddress ?: ""
        ).show(supportFragmentManager, EditTeamDialog.TAG)
    }


    override fun onTeamUpdated(updatedTeam: Team) {
        // Refresh team data after successful edit
        binds.teamNameText.text = updatedTeam.name
        binds.teamDescriptionText.text = updatedTeam.description ?: "No description"

        model.save(updatedTeam)
        // TODO: Update other fields when backend integration is complete
        Clogger.d(TAG, "Team updated: ${updatedTeam.name}")
    }

    companion object {
        private const val TAG = "TeamDetailActivity"
        const val EXTRA_TEAM_ID = "team_id"
        const val EXTRA_TEAM_NAME = "team_name"
    }
} 
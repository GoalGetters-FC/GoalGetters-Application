package com.ggetters.app.ui.management.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ActivityTeamViewerBinding
import com.ggetters.app.ui.management.adapters.TeamViewerAccountAdapter
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import com.ggetters.app.ui.shared.modals.CreateTeamBottomSheet
import com.ggetters.app.ui.shared.modals.JoinTeamBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeamViewerActivity : AppCompatActivity(), Clickable {

    private lateinit var binds: ActivityTeamViewerBinding
    private val model: TeamViewerViewModel by viewModels()
    private lateinit var adapter: TeamViewerAccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d("TeamViewerActivity", "Created new instance")
        setupBindings()
        setupLayoutUi()
        setupRecyclerView()
        setupTouchListeners()
        observe()
    }

    // --- Observe ViewModel state ---
    private fun observe() {
        lifecycleScope.launch {
            model.teams.collectLatest { teams -> adapter.update(teams) }
        }
        lifecycleScope.launch {
            model.toast.collectLatest { msg ->
                Toast.makeText(this@TeamViewerActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
        lifecycleScope.launch {
            model.busy.collectLatest { isBusy ->
                binds.progress?.visibility = if (isBusy) View.VISIBLE else View.GONE
            }
        }
    }

    // --- Recycler Callbacks ---

    private fun onItemOptionSelectClicked(team: Team) {
        model.switchTo(team)
        Toast.makeText(this, "Switched to ${team.name}", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, TeamDetailActivity::class.java).apply {
            putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
            putExtra(TeamDetailActivity.EXTRA_TEAM_NAME, team.name)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
    }

    private fun onItemOptionDeleteClicked(team: Team) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Leave ${team.name}?")
            .setMessage("Are you sure you want to leave this team? This may delete the team if you’re the last player or coach.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Leave") { _, _ ->
                model.leaveTeam(team, currentUserId)
            }
            .show()
    }

    // --- Sheet callbacks ---

    private fun onCreateTeamSheetSubmitted(teamName: String) {
        val authId = FirebaseAuth.getInstance().currentUser?.uid
        if (authId == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }
        model.createTeamFromName(teamName, authId)
    }

    private fun onJoinTeamSheetSubmitted(teamCode: String, userCode: String) {
        model.joinByCode(teamCode, userCode)
    }

    // --- Button Handlers ---

    override fun setupTouchListeners() {
        binds.backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binds.linkTeamButton.setOnClickListener {
            JoinTeamBottomSheet(this::onJoinTeamSheetSubmitted)
                .show(supportFragmentManager, JoinTeamBottomSheet.TAG)
        }

        binds.linkTeamButton.setOnLongClickListener {
            Toast.makeText(this, "Starting sync…", Toast.LENGTH_SHORT).show()
            model.syncTeams()
            true
        }

        binds.createTeamButton.setOnClickListener {
            CreateTeamBottomSheet(this::onCreateTeamSheetSubmitted)
                .show(supportFragmentManager, CreateTeamBottomSheet.TAG)
        }

        binds.createTeamButton.setOnLongClickListener {
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            if (authId == null) {
                Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
            model.createDebugTeam(authId)
            Toast.makeText(this, "Debug team seeded", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // --- UI Setup ---

    private fun setupBindings() {
        binds = ActivityTeamViewerBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()

        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Teams"
        binds.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = TeamViewerAccountAdapter(
            onSelectClicked = this::onItemOptionSelectClicked,
            onDeleteClicked = this::onItemOptionDeleteClicked
        )
        binds.rvAccounts.layoutManager = LinearLayoutManager(this)
        binds.rvAccounts.adapter = adapter
    }

    override fun onClick(view: View?) {
        Clogger.w("TeamViewerActivity", "Unhandled click for: ${view?.id}")
    }
}

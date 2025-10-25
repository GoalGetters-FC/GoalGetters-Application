package com.ggetters.app.ui.management.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.extensions.kotlin.openBrowserTo
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ActivityTeamViewerBinding
import com.ggetters.app.ui.management.adapters.TeamViewerAccountAdapter
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import com.ggetters.app.ui.shared.modals.FormTeamBottomSheet
import com.ggetters.app.ui.shared.modals.JoinTeamBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeamViewerActivity : AppCompatActivity(), Clickable {

    companion object {
        private const val TAG = "TeamViewerActivity"
    }

    private lateinit var binds: ActivityTeamViewerBinding
    private val model: TeamViewerViewModel by viewModels()
    private lateinit var adapter: TeamViewerAccountAdapter

    // --- Lifecycle ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(TAG, "Created new instance of TeamViewerActivity")

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        setupRecyclerView()
        observe()
    }

    // --- Observe ViewModel ---
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
                //binds.progress?.visibility = if (isBusy) View.VISIBLE else View.GONE
                if (isBusy) Toast.makeText(
                    this@TeamViewerActivity, "Syncing...", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Recycler callbacks ---
    private fun onItemClicked(team: Team) {
        val intent = Intent(this, TeamDetailActivity::class.java).apply {
            putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
            putExtra(TeamDetailActivity.EXTRA_TEAM_NAME, team.name)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
    }

    private fun onItemOptionSelectClicked(team: Team) {
        model.switchTo(team)
        Toast.makeText(this, "Switched to ${team.name}", Toast.LENGTH_SHORT).show()
    }

    private fun onItemOptionDeleteClicked(team: Team) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this).setTitle("Leave ${team.name}?")
            .setMessage("Are you sure you want to leave this team? This may delete the team if you’re the last player or coach.")
            .setNegativeButton("Cancel", null).setPositiveButton("Leave") { _, _ ->
                model.leaveTeam(team, currentUserId)
            }.show()
    }

    // --- Sheet callbacks ---
    private fun onFormTeamSheetSubmitted(teamName: String) {
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

    // --- Touch listeners ---
    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
        binds.appBar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binds.bottomBar.setOnMenuItemClickListener { menuItem ->
            Clogger.d(TAG, "Clicked menu-item: ${menuItem.itemId}")
            when (menuItem.itemId) {
                R.id.nav_item_team_viewer_help -> {
                    openBrowserTo("https://help.goalgettersfc.co.za/")
                }

                R.id.nav_item_team_viewer_code -> {
                    JoinTeamBottomSheet(this::onJoinTeamSheetSubmitted).show(
                            supportFragmentManager,
                            JoinTeamBottomSheet.TAG
                        )
                }

                else -> Clogger.w(TAG, "Unhandled menu click: ${menuItem.itemId}")
            }
            true
        }
    }

    // --- Click handling ---
    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            FormTeamBottomSheet(this::onFormTeamSheetSubmitted).show(
                    supportFragmentManager,
                    FormTeamBottomSheet.TAG
                )
        }

        else -> {
            Clogger.w(TAG, "Unhandled click for: ${view?.id}")
        }
    }

    // --- UI setup ---
    private fun setupBindings() {
        binds = ActivityTeamViewerBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()

        setSupportActionBar(binds.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binds.root.setBackgroundColor("#161620".toColorInt())
        WindowCompat.getInsetsController(
            window, window.decorView
        ).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = TeamViewerAccountAdapter(
            onSelectClicked = this::onItemOptionSelectClicked,
            onDeleteClicked = this::onItemOptionDeleteClicked,
            onClick = this::onItemClicked
        )
        binds.rvAccounts.layoutManager = LinearLayoutManager(this)
        binds.rvAccounts.adapter = adapter
    }
}

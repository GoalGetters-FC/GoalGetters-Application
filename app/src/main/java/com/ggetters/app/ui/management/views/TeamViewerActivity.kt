package com.ggetters.app.ui.management.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.databinding.ActivityTeamViewerBinding
import com.ggetters.app.ui.management.adapters.TeamViewerAccountAdapter
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import com.ggetters.app.ui.shared.modals.CreateTeamBottomSheet
import com.ggetters.app.ui.shared.modals.JoinTeamBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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

    // --- Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(TAG, "Created a new instance of the activity")

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        setupRecyclerView()

        observe()
    }

    // --- ViewModel

    private fun observe() {
        lifecycleScope.launch {
            model.teams.collectLatest { teams ->
                adapter.update(teams)
            }
        }

        // ---- NEW: toast + busy collectors ----
        lifecycleScope.launch {
            model.toast.collectLatest { msg ->
                Toast.makeText(this@TeamViewerActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
        lifecycleScope.launch {
            model.busy.collectLatest { isBusy ->
                // If you don't have a progress view yet, add a small ProgressBar with id "progress"
                val progress = binds.root.findViewById<View?>(R.id.progress)
                progress?.visibility = if (isBusy) View.VISIBLE else View.GONE
            }
        }
    }


    // --- Delegates

    private fun onItemOptionSelectClicked(entity: Team) {
        // Navigate to TeamDetailActivity
        val intent = android.content.Intent(this, TeamDetailActivity::class.java)
        intent.putExtra(TeamDetailActivity.EXTRA_TEAM_ID, entity.code)
        intent.putExtra(TeamDetailActivity.EXTRA_TEAM_NAME, entity.name)
        startActivity(intent)
    }

    private fun onItemOptionDeleteClicked(entity: Team) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete ${entity.name}?")
            .setMessage("This removes it locally right away. Online cleanup happens during sync.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                model.deleteTeam(entity)
                Snackbar.make(binds.root, "Deleted ${entity.name}", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }


    private fun onCreateTeamSheetSubmitted(teamName: String) {
        // ---- NEW ----
        model.createTeamFromName(teamName)
    }

    private fun onJoinTeamSheetSubmitted(teamCode: String, userCode: String) {
        // ---- NEW ----
        model.joinByCode(teamCode, userCode)
    }

    // --- Event Handlers

    override fun setupTouchListeners() {
        // Back button
        binds.backButton.setOnClickListener {
            onBackPressed()
        }

        // Link team button
        binds.linkTeamButton.setOnClickListener {
            JoinTeamBottomSheet(
                this::onJoinTeamSheetSubmitted
            ).show(
                supportFragmentManager, JoinTeamBottomSheet.TAG
            )
        }

        // testing long-click for sync
        binds.linkTeamButton.setOnLongClickListener {
            Toast.makeText(this, "Starting sync…", Toast.LENGTH_SHORT).show()
            model.syncTeams()
            true
        }


        // Create team button
        binds.createTeamButton.setOnClickListener {
            CreateTeamBottomSheet(
                this::onCreateTeamSheetSubmitted
            ).show(
                supportFragmentManager, CreateTeamBottomSheet.TAG
            )
        }
    }

    override fun onClick(view: View?) = when (view?.id) {
        else -> {
            Clogger.w(TAG, "Unhandled on-click for: ${view?.id}")
        }
    }

    // --- UI

    private fun setupBindings() {
        binds = ActivityTeamViewerBinding.inflate(layoutInflater)
    }

    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()

        // Setup toolbar
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Teams"

        // Setup toolbar navigation
        binds.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Apply system-bar insets to the root view
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
}
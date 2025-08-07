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
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        setupRecyclerView()

        seed() // T E M P O R A R Y

        observe()
    }


// --- ViewModel


    private fun observe() {
        // TODO: Observe view-model in here if needed
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
        Toast.makeText(
            this, "Delete: ${entity.name}", Toast.LENGTH_SHORT
        ).show()
    }
    

    private fun onCreateTeamSheetSubmitted(teamName: String) {
        // TODO
    }


    private fun onJoinTeamSheetSubmitted(
        teamCode: String, userCode: String
    ) {
        // TODO
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            CreateTeamBottomSheet(
                this::onCreateTeamSheetSubmitted
            ).show(
                supportFragmentManager, CreateTeamBottomSheet.TAG
            )
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
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

        // Setup click listeners
        binds.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup menu click listeners
        binds.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_link_team -> {
                    // TODO: Show link team dialog
                    Clogger.d(TAG, "Link team clicked")
                    true
                }
                R.id.action_join_team -> {
                    // TODO: Show join team dialog
                    Clogger.d(TAG, "Join team clicked")
                    true
                }
                R.id.action_refresh -> {
                    // TODO: Refresh team list
                    Clogger.d(TAG, "Refresh clicked")
                    true
                }
                else -> false
            }
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


// --- Temporary


    private fun seed() {
        adapter.update(
            listOf(
                Team(
                    code = "DEV",
                    name = "Dev Team A",
                    alias = "DVT",
                    description = "Development Team",
                    composition = TeamComposition.UNISEX_MALE,
                    denomination = TeamDenomination.OPEN,
                    yearFormed = "2025",
                    contactCell = "+27123456789",
                    contactMail = "dev@goalgetters.app",
                    clubAddress = "Debug Street, Dev City"
                ), Team(
                    code = "DEV",
                    name = "Dev Team B",
                    alias = "DVT",
                    description = "Development Team",
                    composition = TeamComposition.UNISEX_MALE,
                    denomination = TeamDenomination.OPEN,
                    yearFormed = "2025",
                    contactCell = "+27123456789",
                    contactMail = "dev@goalgetters.app",
                    clubAddress = "Debug Street, Dev City"
                )
            )
        )
    }
}
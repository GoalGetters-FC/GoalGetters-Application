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
        Toast.makeText(
            this, "Select: ${entity.name}", Toast.LENGTH_SHORT
        ).show()
    }


    private fun onItemOptionDeleteClicked(entity: Team) {
        Toast.makeText(
            this, "Delete: ${entity.name}", Toast.LENGTH_SHORT
        ).show()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
        binds.bottomBar.setOnMenuItemClickListener { menuItem ->
            Clogger.d(
                TAG, "Clicked menu-item: ${menuItem.itemId}"
            )

            when (menuItem.itemId) {
                R.id.nav_item_team_viewer_back -> {
                    finish()
                }

                R.id.nav_item_team_viewer_code -> {
                    // TODO: Show team code bottom sheet
                }

                else -> {
                    Clogger.w(
                        TAG, "Unhandled menu-item-on-click for: ${menuItem.itemId}"
                    )

                    false
                }
            }

            true
        }
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            // TODO: Show create team bottom sheet
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

        // Apply system-bar insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }


    private fun setupRecyclerView() {
        adapter = TeamViewerAccountAdapter(
            onSelectClicked = this::onItemOptionSelectClicked,
            onDeleteClicked = this::onItemOptionDeleteClicked
        )

        binds.rvAccounts.adapter = adapter
        binds.rvAccounts.layoutManager = LinearLayoutManager(this)
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
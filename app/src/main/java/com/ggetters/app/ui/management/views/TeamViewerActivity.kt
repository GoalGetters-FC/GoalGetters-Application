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
import com.google.android.material.snackbar.Snackbar
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

        observe()
    }


// --- ViewModel


    private fun observe() {
        lifecycleScope.launch {
            model.teams.collectLatest { teams ->
                adapter.update(teams)
            }
        }

        lifecycleScope.launch {
            model.toast.collectLatest { msg ->
                Toast.makeText(this@TeamViewerActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            model.busy.collectLatest { _ ->
                // Insert progress/loading logic
            }
        }
    }


// --- Delegates


    private fun onItemClicked(entity: Team) {
        val intent = Intent(this, TeamDetailActivity::class.java).apply {
            putExtra(TeamDetailActivity.EXTRA_TEAM_ID, entity.id)
            putExtra(TeamDetailActivity.EXTRA_TEAM_NAME, entity.name)
        }

        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.fade_out
        )
    }


    private fun onItemOptionSelectClicked(entity: Team) {
        model.switchTo(entity)
        Toast.makeText(
            this, "Switched to ${entity.name}", Toast.LENGTH_SHORT
        ).show()
    }


    private fun onItemOptionDeleteClicked(entity: Team) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete ${entity.name}?")
            .setMessage("If you choose to leave the team, you'll have to join back using your team and membership code if you change your mind later.\n\nThis will not delete your profile from that team.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                model.deleteTeam(entity)
                Snackbar.make(
                    binds.root, "Deleted ${entity.name}", Snackbar.LENGTH_SHORT
                ).show()
            }
            .show()
    }


    private fun onFormTeamSheetSubmitted(teamName: String) {
        // TODO: Firebase stuff shouldn't be here, but it does fix this issue for now
        val authId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Toast.makeText(
                this, "Not signed in", Toast.LENGTH_SHORT
            ).show()

        model.createTeamFromName(teamName, authId)
    }


    private fun onJoinTeamSheetSubmitted(teamCode: String, userCode: String) {
        model.joinByCode(teamCode, userCode)
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
        binds.appBar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
        }
        binds.bottomBar.setOnMenuItemClickListener { menuItem ->
            Clogger.d(
                TAG, "Clicked menu-item: ${menuItem.itemId}"
            )

            when (menuItem.itemId) {
                R.id.nav_item_team_viewer_help -> {
                    // Insert help and faq dialog invoker
                }

                R.id.nav_item_team_viewer_code -> {
                    JoinTeamBottomSheet(
                        this::onJoinTeamSheetSubmitted
                    ).show(
                        supportFragmentManager, JoinTeamBottomSheet.TAG
                    )
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
            CreateTeamBottomSheet(
                this::onFormTeamSheetSubmitted
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

        setSupportActionBar(binds.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
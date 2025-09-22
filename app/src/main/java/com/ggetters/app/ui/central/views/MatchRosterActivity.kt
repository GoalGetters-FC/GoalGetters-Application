package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.ui.central.adapters.RosterPlayerAdapter
import com.ggetters.app.ui.central.viewmodels.MatchRosterViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MatchRosterActivity : AppCompatActivity() {

    private val viewModel: MatchRosterViewModel by viewModels()

    // UI Components
    private lateinit var playerStatsText: android.widget.TextView
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var allPlayersChip: Chip
    private lateinit var availableChip: Chip
    private lateinit var maybeChip: Chip
    private lateinit var unavailableChip: Chip
    private lateinit var noResponseChip: Chip
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var setFormationButton: MaterialButton
    private lateinit var notifyPlayersButton: MaterialButton

    private lateinit var playerAdapter: RosterPlayerAdapter
    private var allPlayers = listOf<RosterPlayer>()
    private var filteredPlayers = listOf<RosterPlayer>()
    private var currentFilter: RSVPStatus? = null
    private var showAll = true

    // Match data
    private var matchId = ""
    private var matchTitle = ""
    private var homeTeam = ""
    private var awayTeam = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_roster)

        setupToolbar()
        getMatchDataFromIntent()
        initializeViews()
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        loadPlayerData()
        observeViewModel()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Player Roster"
        }
    }

    private fun getMatchDataFromIntent() {
        matchId = intent.getStringExtra("match_id") ?: ""
        matchTitle = intent.getStringExtra("match_title") ?: "Match"
        homeTeam = intent.getStringExtra("home_team") ?: "Home Team"
        awayTeam = intent.getStringExtra("away_team") ?: "Away Team"
    }

    private fun initializeViews() {
        playerStatsText = findViewById(R.id.playerStatsText)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        allPlayersChip = findViewById(R.id.allPlayersChip)
        availableChip = findViewById(R.id.availableChip)
        maybeChip = findViewById(R.id.maybeChip)
        unavailableChip = findViewById(R.id.unavailableChip)
        noResponseChip = findViewById(R.id.noResponseChip)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        setFormationButton = findViewById(R.id.setFormationButton)
        notifyPlayersButton = findViewById(R.id.notifyPlayersButton)
    }

    private fun setupRecyclerView() {
        playerAdapter = RosterPlayerAdapter(
            onPlayerClick = { player -> showPlayerDetails(player) },
            onRSVPChange = { player, newStatus -> updatePlayerRSVP(player, newStatus) },
            onContactPlayer = { player -> contactPlayer(player) }
        )

        playersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MatchRosterActivity)
            adapter = playerAdapter
        }
    }

    private fun setupFilterChips() {
        allPlayersChip.setOnClickListener { filterPlayers(null) }
        availableChip.setOnClickListener { filterPlayers(RSVPStatus.AVAILABLE) }
        maybeChip.setOnClickListener { filterPlayers(RSVPStatus.MAYBE) }
        unavailableChip.setOnClickListener { filterPlayers(RSVPStatus.UNAVAILABLE) }
        noResponseChip.setOnClickListener { filterPlayers(RSVPStatus.NOT_RESPONDED) }

        allPlayersChip.isChecked = true
    }

    private fun setupClickListeners() {
        setFormationButton.setOnClickListener { navigateToFormation() }
        notifyPlayersButton.setOnClickListener { sendPlayerNotifications() }
    }

    private fun loadPlayerData() {
        viewModel.loadRoster(matchId)
    }

    private fun filterPlayers(status: RSVPStatus?) {
        filteredPlayers = if (status == null) {
            showAll = true
            allPlayers
        } else {
            showAll = false
            currentFilter = status
            allPlayers.filter { it.status == status }
        }

        playerAdapter.updatePlayers(filteredPlayers)
        updateFilterChipAppearance(status)
    }

    private fun updateFilterChipAppearance(selectedStatus: RSVPStatus?) {
        listOf(allPlayersChip, availableChip, maybeChip, unavailableChip, noResponseChip)
            .forEach { it.isChecked = false }

        when (selectedStatus) {
            null -> allPlayersChip.isChecked = true
            RSVPStatus.AVAILABLE -> availableChip.isChecked = true
            RSVPStatus.MAYBE -> maybeChip.isChecked = true
            RSVPStatus.UNAVAILABLE -> unavailableChip.isChecked = true
            RSVPStatus.NOT_RESPONDED -> noResponseChip.isChecked = true
        }
    }

    private fun updatePlayerStats() {
        val available = allPlayers.count { it.status == RSVPStatus.AVAILABLE }
        val maybe = allPlayers.count { it.status == RSVPStatus.MAYBE }
        val unavailable = allPlayers.count { it.status == RSVPStatus.UNAVAILABLE }
        val noResponse = allPlayers.count { it.status == RSVPStatus.NOT_RESPONDED }

        playerStatsText.text =
            "$available available, $maybe maybe, $unavailable unavailable, $noResponse no response"

        availableChip.text = "Available ($available)"
        maybeChip.text = "Maybe ($maybe)"
        unavailableChip.text = "Unavailable ($unavailable)"
        noResponseChip.text = "No Response ($noResponse)"
        allPlayersChip.text = "All (${allPlayers.size})"

        setFormationButton.isEnabled = available >= 11
        setFormationButton.text =
            if (available >= 11) "Set Formation" else "Need ${11 - available} more players"
    }

    private fun updatePlayerRSVP(player: RosterPlayer, newStatus: RSVPStatus) {
        viewModel.updatePlayerRSVP(matchId, player.playerId, newStatus)
    }

    private fun showPlayerDetails(player: RosterPlayer) {
        Snackbar.make(findViewById(android.R.id.content),
            "Player details for ${player.playerName}", Snackbar.LENGTH_SHORT).show()
    }

    private fun contactPlayer(player: RosterPlayer) {
        Snackbar.make(findViewById(android.R.id.content),
            "Contact ${player.playerName} - feature coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToFormation() {
        val availablePlayersCount = allPlayers.count { it.status == RSVPStatus.AVAILABLE }

        if (availablePlayersCount >= 11) {
            val intent = Intent(this, FormationActivity::class.java).apply {
                putExtra("match_id", matchId)
                putExtra("match_title", matchTitle)
                putExtra("home_team", homeTeam)
                putExtra("away_team", awayTeam)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                "Need ${11 - availablePlayersCount} more available players to set formation",
                Snackbar.LENGTH_LONG).show()
        }
    }

    private fun sendPlayerNotifications() {
        val noResponsePlayers = allPlayers.filter { it.status == RSVPStatus.NOT_RESPONDED }
        if (noResponsePlayers.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                "All players have responded", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                "Notifications sent to ${noResponsePlayers.size} players", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.players.collect { players ->
                allPlayers = players
                filterPlayers(currentFilter)
                updatePlayerStats()
            }
        }
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_roster, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshPlayerData(); true
            }
            R.id.action_export -> {
                exportRosterData(); true
            }
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshPlayerData() {
        viewModel.loadRoster(matchId)
        Snackbar.make(findViewById(android.R.id.content),
            "Player data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    private fun exportRosterData() {
        Snackbar.make(findViewById(android.R.id.content),
            "Export feature coming soon", Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        refreshPlayerData()
    }
}

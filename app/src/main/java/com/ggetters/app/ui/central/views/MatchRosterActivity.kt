package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAvailabilityAdapter
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.viewmodels.MatchRosterViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement real-time player availability synchronization
// TODO: Backend - Add player availability notifications and reminders
// TODO: Backend - Implement bulk RSVP updates and messaging
// TODO: Backend - Add player contact integration (call/message)
// TODO: Backend - Implement availability history and analytics
// TODO: Backend - Add squad rotation and player fatigue tracking
// TODO: Backend - Implement automated lineup suggestions based on availability

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
    
    private lateinit var playerAdapter: PlayerAvailabilityAdapter
    private var allPlayers = listOf<PlayerAvailability>()
    private var filteredPlayers = listOf<PlayerAvailability>()
    private var currentFilter = RSVPStatus.AVAILABLE // Show all by default
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
        playerAdapter = PlayerAvailabilityAdapter(
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
        
        // Set default filter
        allPlayersChip.isChecked = true
    }

    private fun setupClickListeners() {
        setFormationButton.setOnClickListener {
            navigateToFormation()
        }
        
        notifyPlayersButton.setOnClickListener {
            sendPlayerNotifications()
        }
    }

    private fun loadPlayerData() {
        // TODO: Backend - Load real player data from backend
        allPlayers = createSamplePlayerData()
        filterPlayers(null) // Show all players initially
        updatePlayerStats()
    }

    private fun createSamplePlayerData(): List<PlayerAvailability> {
        // TODO: Backend - Replace with real data from repository
        return listOf(
            PlayerAvailability("1", "John Smith", "GK", 1, RSVPStatus.AVAILABLE),
            PlayerAvailability("2", "Mike Johnson", "CB", 4, RSVPStatus.AVAILABLE),
            PlayerAvailability("3", "David Wilson", "CB", 5, RSVPStatus.AVAILABLE),
            PlayerAvailability("4", "Chris Brown", "LB", 3, RSVPStatus.MAYBE),
            PlayerAvailability("5", "Tom Davis", "RB", 2, RSVPStatus.AVAILABLE),
            PlayerAvailability("6", "Alex Miller", "CM", 8, RSVPStatus.AVAILABLE),
            PlayerAvailability("7", "Sam Wilson", "CM", 6, RSVPStatus.UNAVAILABLE),
            PlayerAvailability("8", "Jake Taylor", "CM", 10, RSVPStatus.AVAILABLE),
            PlayerAvailability("9", "Ben Moore", "LW", 11, RSVPStatus.AVAILABLE),
            PlayerAvailability("10", "Luke Jackson", "ST", 9, RSVPStatus.AVAILABLE),
            PlayerAvailability("11", "Ryan White", "RW", 7, RSVPStatus.MAYBE),
            PlayerAvailability("12", "Mark Lewis", "SUB", 12, RSVPStatus.AVAILABLE),
            PlayerAvailability("13", "Paul Clark", "SUB", 13, RSVPStatus.NOT_RESPONDED),
            PlayerAvailability("14", "Steve Hall", "SUB", 14, RSVPStatus.NOT_RESPONDED),
            PlayerAvailability("15", "Nick Allen", "SUB", 15, RSVPStatus.UNAVAILABLE),
            PlayerAvailability("16", "James Garcia", "SUB", 16, RSVPStatus.AVAILABLE),
            PlayerAvailability("17", "Daniel Martinez", "SUB", 17, RSVPStatus.MAYBE),
            PlayerAvailability("18", "Matthew Rodriguez", "SUB", 18, RSVPStatus.NOT_RESPONDED)
        )
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
        // Reset all chips
        listOf(allPlayersChip, availableChip, maybeChip, unavailableChip, noResponseChip)
            .forEach { it.isChecked = false }
        
        // Set selected chip
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
        
        playerStatsText.text = "$available available, $maybe maybe, $unavailable unavailable, $noResponse no response"
        
        // Update chip counts
        availableChip.text = "Available ($available)"
        maybeChip.text = "Maybe ($maybe)"
        unavailableChip.text = "Unavailable ($unavailable)"
        noResponseChip.text = "No Response ($noResponse)"
        allPlayersChip.text = "All (${allPlayers.size})"
        
        // Update button states
        setFormationButton.isEnabled = available >= 11
        setFormationButton.text = if (available >= 11) {
            "Set Formation"
        } else {
            "Need ${11 - available} more players"
        }
    }

    private fun updatePlayerRSVP(player: PlayerAvailability, newStatus: RSVPStatus) {
        // TODO: Backend - Update player RSVP in backend
        
        // Update local data
        allPlayers = allPlayers.map { 
            if (it.playerId == player.playerId) {
                it.copy(status = newStatus, responseTime = java.util.Date())
            } else {
                it
            }
        }
        
        // Refresh the current filter
        if (showAll) {
            filterPlayers(null)
        } else {
            filterPlayers(currentFilter)
        }
        
        updatePlayerStats()
        
        Snackbar.make(findViewById(android.R.id.content), 
            "${player.playerName} marked as ${newStatus.name.lowercase()}", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun showPlayerDetails(player: PlayerAvailability) {
        // TODO: Backend - Navigate to player details screen
        Snackbar.make(findViewById(android.R.id.content), 
            "Player details for ${player.playerName}", Snackbar.LENGTH_SHORT).show()
    }

    private fun contactPlayer(player: PlayerAvailability) {
        // TODO: Backend - Implement player contact (call/message)
        Snackbar.make(findViewById(android.R.id.content), 
            "Contact ${player.playerName} - feature coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToFormation() {
        // TODO: Backend - Navigate to formation setup
        val availablePlayersCount = allPlayers.count { it.status == RSVPStatus.AVAILABLE }
        
        if (availablePlayersCount >= 11) {
            val intent = Intent(this, FormationActivity::class.java).apply {
                putExtra("match_id", matchId)
                putExtra("match_title", matchTitle)
                putExtra("home_team", homeTeam)
                putExtra("away_team", awayTeam)
                // TODO: Pass available players as serializable
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
        // TODO: Backend - Send notifications to players who haven't responded
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
        // TODO: Backend - Observe player data changes from ViewModel
        // TODO: Backend - Handle loading states and errors
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_roster, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshPlayerData()
                true
            }
            R.id.action_export -> {
                exportRosterData()
                true
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
        // TODO: Backend - Refresh player data from backend
        loadPlayerData()
        Snackbar.make(findViewById(android.R.id.content), 
            "Player data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    private fun exportRosterData() {
        // TODO: Backend - Export roster data (CSV, PDF, etc.)
        Snackbar.make(findViewById(android.R.id.content), 
            "Export feature coming soon", Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // TODO: Backend - Refresh data when returning from other screens
        refreshPlayerData()
    }
}

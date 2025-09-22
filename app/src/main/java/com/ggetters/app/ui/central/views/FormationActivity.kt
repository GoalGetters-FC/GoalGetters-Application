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
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.ui.central.adapters.FormationPlayerAdapter
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.viewmodels.FormationViewModel
import com.ggetters.app.ui.central.views.components.PitchView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement formation template saving and loading
// TODO: Backend - Add formation analytics and recommendations
// TODO: Backend - Implement team formation history and patterns
// TODO: Backend - Add formation sharing between coaches
// TODO: Backend - Implement player position preferences and compatibility
// TODO: Backend - Add formation validation rules and constraints
// TODO: Backend - Implement automated formation suggestions based on available players

@AndroidEntryPoint
class FormationActivity : AppCompatActivity() {

    private val viewModel: FormationViewModel by viewModels()
    
    // UI Components
    private lateinit var formationChipGroup: ChipGroup
    private lateinit var formation433Chip: Chip
    private lateinit var formation442Chip: Chip
    private lateinit var formation352Chip: Chip
    private lateinit var formation451Chip: Chip
    private lateinit var pitchView: PitchView
    private lateinit var availablePlayersRecyclerView: RecyclerView
    private lateinit var startMatchButton: MaterialButton
    private lateinit var saveFormationButton: MaterialButton
    private lateinit var formationStatsText: android.widget.TextView
    
    private lateinit var playerAdapter: FormationPlayerAdapter
    private var availablePlayers = listOf<RosterPlayer>()
    private var selectedFormation = "4-3-3"
    private var positionedPlayers = mutableMapOf<String, RosterPlayer>() // position -> player
    
    // Match data
    private var matchId = ""
    private var matchTitle = ""
    private var homeTeam = ""
    private var awayTeam = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formation)
        
        setupToolbar()
        getMatchDataFromIntent()
        initializeViews()
        setupFormationChips()
        setupRecyclerView()
        setupPitchView()
        setupClickListeners()
        loadPlayerData()
        observeViewModel()
        setDefaultFormation()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Formation Setup"
        }
    }

    private fun getMatchDataFromIntent() {
        matchId = intent.getStringExtra("match_id") ?: ""
        matchTitle = intent.getStringExtra("match_title") ?: "Match"
        homeTeam = intent.getStringExtra("home_team") ?: "Home Team"
        awayTeam = intent.getStringExtra("away_team") ?: "Away Team"
    }

    private fun initializeViews() {
        formationChipGroup = findViewById(R.id.formationChipGroup)
        formation433Chip = findViewById(R.id.formation433Chip)
        formation442Chip = findViewById(R.id.formation442Chip)
        formation352Chip = findViewById(R.id.formation352Chip)
        formation451Chip = findViewById(R.id.formation451Chip)
        pitchView = findViewById(R.id.pitchView)
        availablePlayersRecyclerView = findViewById(R.id.availablePlayersRecyclerView)
        startMatchButton = findViewById(R.id.startMatchButton)
        saveFormationButton = findViewById(R.id.saveFormationButton)
        formationStatsText = findViewById(R.id.formationStatsText)
    }

    private fun setupFormationChips() {
        formation433Chip.setOnClickListener { selectFormation("4-3-3") }
        formation442Chip.setOnClickListener { selectFormation("4-4-2") }
        formation352Chip.setOnClickListener { selectFormation("3-5-2") }
        formation451Chip.setOnClickListener { selectFormation("4-5-1") }
        
        // Set default selection
        formation433Chip.isChecked = true
    }

    private fun setupRecyclerView() {
        playerAdapter = FormationPlayerAdapter(
            onPlayerDragStart = { player -> handlePlayerDragStart(player) },
            onPlayerClick = { player -> showPlayerDetails(player) }
        )
        
        availablePlayersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FormationActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerAdapter
        }
    }

    private fun setupPitchView() {
        pitchView.apply {
            onPositionClick = { position -> handlePositionClick(position) }
            onPlayerDrop = { position, player -> handlePlayerDrop(position, player) }
            onPlayerRemove = { position -> handlePlayerRemove(position) }
        }
    }

    private fun setupClickListeners() {
        startMatchButton.setOnClickListener {
            if (isFormationValid()) {
                startMatch()
            } else {
                showFormationIncompleteMessage()
            }
        }
        
        saveFormationButton.setOnClickListener {
            saveFormation()
        }
    }

    private fun loadPlayerData() {
        // TODO: Backend - Load available players for this match
        availablePlayers = createSampleAvailablePlayers()
        playerAdapter.updatePlayers(availablePlayers)
        updateFormationStats()
    }

    private fun createSampleAvailablePlayers(): List<RosterPlayer> {
        // TODO: Backend - Get from intent or repository
        return listOf(
            RosterPlayer("1", "John Smith", 1,"GK", RSVPStatus.AVAILABLE),
            RosterPlayer("2", "Mike Johnson", 11,"CB", RSVPStatus.AVAILABLE),
        )
    }

    private fun selectFormation(formation: String) {
        selectedFormation = formation
        updateFormationChipsAppearance()
        pitchView.setFormation(formation)
        clearAllPositions()
        updateFormationStats()
        
        Snackbar.make(findViewById(android.R.id.content), 
            "Formation set to $formation", Snackbar.LENGTH_SHORT).show()
    }

    private fun updateFormationChipsAppearance() {
        // Reset all chips
        listOf(formation433Chip, formation442Chip, formation352Chip, formation451Chip)
            .forEach { it.isChecked = false }
        
        // Set selected chip
        when (selectedFormation) {
            "4-3-3" -> formation433Chip.isChecked = true
            "4-4-2" -> formation442Chip.isChecked = true
            "3-5-2" -> formation352Chip.isChecked = true
            "4-5-1" -> formation451Chip.isChecked = true
        }
    }

    private fun setDefaultFormation() {
        selectFormation("4-3-3")
    }

    private fun handlePlayerDragStart(player: RosterPlayer) {
        // TODO: Backend - Handle drag start analytics
        Snackbar.make(findViewById(android.R.id.content), 
            "Drag ${player.playerName} to a position on the pitch", Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePositionClick(position: String) {
        // TODO: Backend - Show position selection dialog or auto-assign best player
        if (positionedPlayers.containsKey(position)) {
            val player = positionedPlayers[position]
            Snackbar.make(findViewById(android.R.id.content), 
                "Position $position: ${player?.playerName}", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(findViewById(android.R.id.content), 
                "Position $position is empty", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handlePlayerDrop(position: String, player: RosterPlayer) {
        // TODO: Backend - Validate position compatibility with player
        
        // Remove player from previous position if any
        positionedPlayers.values.find { it.playerId == player.playerId }?.let {
            val previousPosition = positionedPlayers.entries.find { entry -> entry.value.playerId == player.playerId }?.key
            previousPosition?.let { pos -> positionedPlayers.remove(pos) }
        }
        
        // Add player to new position
        positionedPlayers[position] = player
        
        // Update pitch view
        pitchView.updatePlayerPosition(position, player)
        
        // Remove from available players temporarily
        playerAdapter.updatePlayers(availablePlayers.filter { it.playerId != player.playerId })
        
        updateFormationStats()
        
        Snackbar.make(findViewById(android.R.id.content), 
            "${player.playerName} positioned at $position", Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePlayerRemove(position: String) {
        positionedPlayers[position]?.let { player ->
            positionedPlayers.remove(position)
            pitchView.removePlayerFromPosition(position)
            
            // Add back to available players
            playerAdapter.updatePlayers(availablePlayers.filter { positioned ->
                positionedPlayers.values.none { it.playerId == positioned.playerId }
            })
            
            updateFormationStats()
            
            Snackbar.make(findViewById(android.R.id.content), 
                "${player.playerName} removed from $position", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun clearAllPositions() {
        positionedPlayers.clear()
        pitchView.clearAllPositions()
        playerAdapter.updatePlayers(availablePlayers)
        updateFormationStats()
    }

    private fun updateFormationStats() {
        val positionedCount = positionedPlayers.size
        val requiredCount = 11
        val availableCount = availablePlayers.size - positionedCount
        
        formationStatsText.text = "$positionedCount / $requiredCount players positioned, $availableCount available"
        
        // Update button states
        startMatchButton.isEnabled = isFormationValid()
        startMatchButton.text = if (isFormationValid()) {
            "Start Match"
        } else {
            "Need ${requiredCount - positionedCount} more players"
        }
        
        saveFormationButton.isEnabled = positionedCount > 0
    }

    private fun isFormationValid(): Boolean {
        return positionedPlayers.size == 11
    }

    private fun showPlayerDetails(player: RosterPlayer) {
        // TODO: Backend - Show player details dialog
        Snackbar.make(findViewById(android.R.id.content), 
            "${player.playerName} - ${player.position} (#${player.jerseyNumber})", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun showFormationIncompleteMessage() {
        val needed = 11 - positionedPlayers.size
        Snackbar.make(findViewById(android.R.id.content), 
            "Please position $needed more players before starting the match", 
            Snackbar.LENGTH_LONG).show()
    }

    private fun startMatch() {
        // TODO: Backend - Save formation and start match
        val intent = Intent(this, MatchControlActivity::class.java).apply {
            putExtra("event_id", matchId)
            putExtra("event_title", matchTitle)
            putExtra("event_opponent", awayTeam)
            putExtra("home_team", homeTeam)
            // TODO: Pass formation data
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        
        Snackbar.make(findViewById(android.R.id.content), 
            "Starting match with $selectedFormation formation!", Snackbar.LENGTH_SHORT).show()
    }

    private fun saveFormation() {
        // TODO: Backend - Save formation template
        Snackbar.make(findViewById(android.R.id.content), 
            "Formation saved successfully", Snackbar.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        // TODO: Backend - Observe formation data changes from ViewModel
        // TODO: Backend - Handle loading states and errors
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_formation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_auto_fill -> {
                autoFillFormation()
                true
            }
            R.id.action_clear_all -> {
                clearAllPositions()
                true
            }
            R.id.action_load_template -> {
                loadFormationTemplate()
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

    private fun autoFillFormation() {
        // TODO: Backend - Implement intelligent auto-fill based on player positions
        Snackbar.make(findViewById(android.R.id.content), 
            "Auto-fill formation coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun loadFormationTemplate() {
        // TODO: Backend - Load saved formation templates
        Snackbar.make(findViewById(android.R.id.content), 
            "Formation templates coming soon", Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // TODO: Backend - Refresh formation data when returning
        updateFormationStats()
    }
}


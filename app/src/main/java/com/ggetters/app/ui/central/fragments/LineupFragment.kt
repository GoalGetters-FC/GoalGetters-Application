package com.ggetters.app.ui.central.fragments

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ggetters.app.core.utils.Clogger
import kotlinx.coroutines.flow.collectLatest
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.LineupPlayerGridAdapter
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.ui.central.viewmodels.LineupViewModel
import com.ggetters.app.ui.central.viewmodels.MatchRosterViewModel
import com.ggetters.app.ui.central.views.components.FormationPitchView
import com.ggetters.app.ui.central.sheets.MatchEventsBottomSheet
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement formation management
// TODO: Backend - Add player position assignments  
// TODO: Backend - Implement drag-and-drop player positioning
// TODO: Backend - Add formation templates and presets

@AndroidEntryPoint
class LineupFragment : Fragment() {

    private val viewModel: LineupViewModel by viewModels()
    private val rosterViewModel: MatchRosterViewModel by viewModels()
    
    // Arguments
    private var matchId: String = ""
    private var matchTitle: String = ""
    private var homeTeam: String = ""
    private var awayTeam: String = ""
    
    // UI Components
    private lateinit var formationSpinner: Spinner
    private lateinit var searchButton: ImageButton
    private lateinit var pitchView: FormationPitchView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var fabMatchEvents: FloatingActionButton
    
    // Adapters
    private lateinit var playersAdapter: LineupPlayerGridAdapter
    
    // Data
    private var availablePlayers = listOf<RosterPlayer>()
    private var currentFormation = "4-3-3"
    private var positionedPlayers = mutableMapOf<String, RosterPlayer?>()
    private var currentDraggedPlayer: RosterPlayer? = null
    private val formations = listOf("4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2")

    companion object {
        fun newInstance(eventId: String): LineupFragment {
            val fragment = LineupFragment()
            fragment.arguments = Bundle().apply {
                putString("event_id", eventId)
            }
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString("event_id", "")  // Fixed: use "event_id" to match newInstance
            matchTitle = it.getString("match_title", "")
            homeTeam = it.getString("home_team", "")
            awayTeam = it.getString("away_team", "")
        }
        Clogger.d("LineupFragment", "onCreate: matchId='$matchId'")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lineup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupFormationSpinner()
        setupPlayersGrid()
        loadPlayerData()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        formationSpinner = view.findViewById(R.id.formationSpinner)
        searchButton = view.findViewById(R.id.searchButton)
        pitchView = view.findViewById(R.id.pitchView)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)
        fabMatchEvents = view.findViewById(R.id.fabMatchEvents)
        
        // Search button click
        searchButton.setOnClickListener {
            showPlayerSearch()
        }
        
        // FAB click - track match events
        fabMatchEvents.setOnClickListener {
            showMatchEventsBottomSheet()
        }
        
        // Setup pitch interactions
        setupPitchInteractions()
    }

    private fun setupPitchInteractions() {
        // Player dropped - handle position change
        pitchView.setOnPlayerDroppedListener { position, dropPoint ->
            android.util.Log.d("LineupFragment", "Player dropped listener called: position=$position, dropPoint=(${dropPoint.x}, ${dropPoint.y})")
            handlePlayerDrop(position, dropPoint)
        }
        // Click interactions
        pitchView.setOnPlayerClickListener { position, player ->
            if (player != null) {
                showPlayerOptionsDialog(position, player)
            }
        }
        pitchView.setOnPositionClickListener { position ->
            showPlayerSelectionDialog(position)
        }
        
        android.util.Log.d("LineupFragment", "Pitch interactions setup completed")
    }

    private fun setupFormationSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_formation_spinner,
            formations
        )
        adapter.setDropDownViewResource(R.layout.item_formation_spinner_dropdown)
        formationSpinner.adapter = adapter
        
        // Set default formation
        val defaultIndex = formations.indexOf(currentFormation)
        if (defaultIndex >= 0) {
            formationSpinner.setSelection(defaultIndex)
        }
        
        formationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newFormation = formations[position]
                if (newFormation != currentFormation) {
                    changeFormation(newFormation)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupPlayersGrid() {
        playersAdapter = LineupPlayerGridAdapter(
            onPlayerClick = { player -> handlePlayerSelection(player) },
            onAddPlayerClick = { showAddPlayer() },
            onPlayerDragStart = { player -> handlePlayerDragStart(player) }
        )
        
        // Grid layout with 4 columns as shown in sketch
        val gridLayoutManager = GridLayoutManager(requireContext(), 4)
        playersRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = playersAdapter
        }
    }

    private fun loadPlayerData() {
        Clogger.d("LineupFragment", "loadPlayerData: matchId='$matchId'")
        // Load roster data from MatchRosterViewModel - this will get real attendance data
        rosterViewModel.loadRoster(matchId)
        // Load saved lineup (formation + positioned players)
        viewModel.loadLineup(matchId)
        
        // Set initial formation on pitch
        updatePitchFormation()
        
        // Update players grid (exclude players on pitch)
        updateAvailablePlayersGrid()
    }

    private fun changeFormation(newFormation: String) {
        currentFormation = newFormation
        // Persist via ViewModel
        viewModel.updateFormation(newFormation)
        updatePitchFormation()
        
        // Update available players grid after formation change
        updateAvailablePlayersGrid()
        
        Snackbar.make(requireView(), 
            "Formation changed to $newFormation", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun updatePitchFormation() {
        // Set formation and update pitch view
        pitchView.setFormation(currentFormation)
        
        // Only auto-position players if no players are currently positioned and we have available players
        val currentPositions = pitchView.getPositionedPlayers()
        if ((currentPositions.isEmpty() || currentPositions.values.all { it == null }) && availablePlayers.isNotEmpty()) {
            // Auto-position available players based on formation
            when (currentFormation) {
                "4-3-3" -> setup433Formation()
                "4-4-2" -> setup442Formation()
                "3-5-2" -> setup352Formation()
                "4-2-3-1" -> setup4231Formation()
                "5-3-2" -> setup532Formation()
            }
        }
    }
    
    private fun findPlayerByPosition(position1: String, position2: String, index: Int = 0): RosterPlayer? {
        val matchingPlayers = availablePlayers.filter { player ->
            player.position.contains(position1, ignoreCase = true) || 
            player.position.contains(position2, ignoreCase = true)
        }
        return matchingPlayers.getOrNull(index)
    }

    private fun setup433Formation() {
        // Auto-position available players in 4-3-3 formation based on their positions
        val formationPlayers = mapOf(
            "GK" to findPlayerByPosition("GK", "Goalkeeper"),
            "LB" to findPlayerByPosition("LB", "Left"),
            "CB1" to findPlayerByPosition("CB", "Center"),
            "CB2" to findPlayerByPosition("CB", "Center", 1),
            "RB" to findPlayerByPosition("RB", "Right"),
            "CM1" to findPlayerByPosition("CM", "Midfielder"),
            "CM2" to findPlayerByPosition("CM", "Midfielder", 1),
            "CM3" to findPlayerByPosition("CM", "Midfielder", 2),
            "LW" to findPlayerByPosition("LW", "Winger"),
            "ST" to findPlayerByPosition("ST", "Striker"),
            "RW" to findPlayerByPosition("RW", "Winger", 1)
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun setup442Formation() {
        // 4-4-2 formation setup
        val formationPlayers = mapOf(
            "GK" to findPlayerByPosition("GK", "Goalkeeper"),
            "LB" to findPlayerByPosition("LB", "Left"),
            "CB1" to findPlayerByPosition("CB", "Center"),
            "CB2" to findPlayerByPosition("CB", "Center", 1),
            "RB" to findPlayerByPosition("RB", "Right"),
            "LM" to findPlayerByPosition("CM", "Midfielder"),
            "CM1" to findPlayerByPosition("CM", "Midfielder", 1),
            "CM2" to findPlayerByPosition("CM", "Midfielder", 2),
            "RM" to findPlayerByPosition("CM", "Midfielder", 3),
            "ST1" to findPlayerByPosition("ST", "Striker"),
            "ST2" to (findPlayerByPosition("ST", "Striker", 1) ?: findPlayerByPosition("FW", "Forward"))
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun setup352Formation() {
        // 3-5-2 formation setup
        val formationPlayers = mapOf(
            "GK" to availablePlayers.find { it.position == "GK" },
            "CB1" to availablePlayers.find { it.position == "CB" },
            "CB2" to availablePlayers.filter { it.position == "CB" }.getOrNull(1),
            "CB3" to availablePlayers.filter { it.position == "CB" }.getOrNull(2),
            "LWB" to availablePlayers.find { it.position == "LB" },
            "CM1" to availablePlayers.find { it.position == "CM" },
            "CM2" to availablePlayers.filter { it.position == "CM" }.getOrNull(1),
            "CM3" to availablePlayers.filter { it.position == "CM" }.getOrNull(2),
            "RWB" to availablePlayers.find { it.position == "RB" },
            "ST1" to availablePlayers.find { it.position == "ST" },
            "ST2" to (availablePlayers.filter { it.position == "ST" }.getOrNull(1) ?: availablePlayers.find { it.position == "FW" })
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun setup4231Formation() {
        // 4-2-3-1 formation setup
        val formationPlayers = mapOf(
            "GK" to availablePlayers.find { it.position == "GK" },
            "LB" to availablePlayers.find { it.position == "LB" },
            "CB1" to availablePlayers.find { it.position == "CB" },
            "CB2" to availablePlayers.filter { it.position == "CB" }.getOrNull(1),
            "RB" to availablePlayers.find { it.position == "RB" },
            "CDM1" to availablePlayers.find { it.position == "CM" },
            "CDM2" to availablePlayers.filter { it.position == "CM" }.getOrNull(1),
            "LW" to availablePlayers.find { it.position == "FW" },
            "CAM" to availablePlayers.filter { it.position == "CM" }.getOrNull(2),
            "RW" to availablePlayers.filter { it.position == "FW" }.getOrNull(1),
            "ST" to availablePlayers.find { it.position == "ST" }
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun setup532Formation() {
        // 5-3-2 formation setup
        val formationPlayers = mapOf(
            "GK" to availablePlayers.find { it.position == "GK" },
            "LB" to availablePlayers.find { it.position == "LB" },
            "CB1" to availablePlayers.find { it.position == "CB" },
            "CB2" to availablePlayers.filter { it.position == "CB" }.getOrNull(1),
            "CB3" to availablePlayers.filter { it.position == "CB" }.getOrNull(2),
            "RB" to availablePlayers.find { it.position == "RB" },
            "CM1" to availablePlayers.find { it.position == "CM" },
            "CM2" to availablePlayers.filter { it.position == "CM" }.getOrNull(1),
            "CM3" to availablePlayers.filter { it.position == "CM" }.getOrNull(2),
            "ST1" to availablePlayers.find { it.position == "ST" },
            "ST2" to (availablePlayers.filter { it.position == "ST" }.getOrNull(1) ?: availablePlayers.find { it.position == "FW" })
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun handlePlayerSelection(player: RosterPlayer) {
        // Show position selection dialog for the selected player
        val availablePositions = pitchView.getAvailablePositions().filter { position ->
            !pitchView.isPositionOccupied(position)
        }
        
        if (availablePositions.isEmpty()) {
            Snackbar.make(requireView(), "All positions are occupied", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val positionNames = availablePositions.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Position ${player.playerName}")
            .setItems(positionNames) { _, which ->
                val selectedPosition = availablePositions[which]
                positionPlayer(player, selectedPosition)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun handlePlayerDragStart(player: RosterPlayer) {
        currentDraggedPlayer = player
        // Player drag started - could add visual feedback here
        // The actual drop handling is done in FormationPitchView's drag listener
        
        // Debug logging
        android.util.Log.d("LineupFragment", "Player drag started: ${player.playerName}")
        
        // Show visual feedback
        Snackbar.make(requireView(), 
            "Dragging ${player.playerName} - drop on pitch to position", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun showAddPlayer() {
        // TODO: Implement add player functionality
        Snackbar.make(requireView(), 
            "Add player functionality coming soon", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun showPlayerSearch() {
        // TODO: Implement player search functionality
        Snackbar.make(requireView(), 
            "Player search coming soon", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        // Observe roster data changes - filter for PRESENT players and track substituted players
        // This connects attendance data to lineup: only players marked as "Present" in attendance
        // will appear in the lineup bench and be available for positioning on the pitch
        // Substituted players (UNAVAILABLE status) will be shown at the bottom of the bench, greyed out
        lifecycleScope.launchWhenStarted {
            rosterViewModel.players.collectLatest { players ->
                Clogger.d("LineupFragment", "Received ${players.size} players from roster")
                
                // Get the previous list of available players to detect changes
                val previousAvailablePlayers = availablePlayers.toSet()
                
                // Filter for players marked as AVAILABLE or UNAVAILABLE (substituted)
                // Attendance status mapping: 0=Present/Available, 2=Unavailable (substituted)
                availablePlayers = players.filter { player ->
                    val isAvailableOrSubstituted = player.status == RSVPStatus.AVAILABLE || player.status == RSVPStatus.UNAVAILABLE
                    Clogger.d("LineupFragment", "Player ${player.playerName}: status=${player.status}, available=$isAvailableOrSubstituted")
                    isAvailableOrSubstituted
                }
                
                val currentAvailablePlayers = availablePlayers.toSet()
                Clogger.d("LineupFragment", "Filtered to ${availablePlayers.size} available/substituted players")
                
                // Handle substitutions by auto-positioning the substitute player
                handleSubstitutionChanges(previousAvailablePlayers, currentAvailablePlayers)
                
                updateAvailablePlayersGrid()
                updatePitchFormation()
            }
        }
        
        // Observe lineup data changes
        lifecycleScope.launchWhenStarted {
            viewModel.players.collectLatest { players ->
                // Update available players if needed
                updateAvailablePlayersGrid()
            }
        }
        
        // Observe formation changes
        lifecycleScope.launchWhenStarted {
            viewModel.formation.collectLatest { formation ->
                currentFormation = formation
                // reflect spinner if changed externally
                formations.indexOf(formation).takeIf { it >= 0 }?.let { idx ->
                    if (formationSpinner.selectedItemPosition != idx) {
                        formationSpinner.setSelection(idx)
                    }
                }
                updatePitchFormation()
            }
        }
        
        // Observe positioned players
        lifecycleScope.launchWhenStarted {
            viewModel.positionedPlayers.collectLatest { newPositionedPlayers ->
                positionedPlayers = newPositionedPlayers.toMutableMap()
                // push to canvas
                pitchView.setPlayers(newPositionedPlayers)
                updateAvailablePlayersGrid()
            }
        }
    }

    // Public method to get current formation
    fun getCurrentFormation(): String = currentFormation

    // Public method to get positioned players
    fun getPositionedPlayers(): Map<String, RosterPlayer?> {
        return pitchView.getPositionedPlayers()
    }

    // Dialog methods for player management
    private fun showPlayerOptionsDialog(position: String, player: RosterPlayer) {
        val options = arrayOf(
            "Change Player",
            "Remove from Position", 
            "View Player Details",
            "Change Role"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("${player.playerName} - $position")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPlayerSelectionDialog(position) // Change player
                    1 -> removePlayerFromPosition(position) // Remove
                    2 -> viewPlayerDetails(player) // View details
                    3 -> showRoleSelectionDialog(position, player) // Change role
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlayerSelectionDialog(position: String) {
        val availablePlayersForPosition = availablePlayers.filter { player ->
            // Only show players not already positioned
            !pitchView.getPositionedPlayers().values.contains(player)
        }
        
        if (availablePlayersForPosition.isEmpty()) {
            Snackbar.make(requireView(), "No available players", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val playerNames = availablePlayersForPosition.map { "${it.playerName} (${it.position})" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Player for $position")
            .setItems(playerNames) { _, which ->
                val selectedPlayer = availablePlayersForPosition[which]
                positionPlayer(selectedPlayer, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRoleSelectionDialog(position: String, player: RosterPlayer) {
        val roles = when (position.take(2)) {
            "GK" -> arrayOf("Goalkeeper", "Sweeper Keeper")
            "CB" -> arrayOf("Center Back", "Ball Playing Defender", "Stopper")
            "LB", "RB" -> arrayOf("Full Back", "Wing Back", "Defensive Wing Back")
            "CM" -> arrayOf("Central Midfielder", "Deep Lying Playmaker", "Box to Box")
            "LW", "RW" -> arrayOf("Winger", "Inside Forward", "Wide Midfielder")
            "ST" -> arrayOf("Striker", "False 9", "Target Man", "Poacher")
            else -> arrayOf("Default Role")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose Role for ${player.playerName}")
            .setItems(roles) { _, which ->
                val selectedRole = roles[which]
                updatePlayerRole(position, player, selectedRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleSubstitutionChanges(previousAvailable: Set<RosterPlayer>, currentAvailable: Set<RosterPlayer>) {
        // Detect substitutions: a player who was available and is now unavailable
        val previouslyAvailableById = previousAvailable.associateBy { it.playerId }
        val currentlyAvailableById = currentAvailable.associateBy { it.playerId }
        
        // Find players whose status changed from AVAILABLE to UNAVAILABLE (substituted out)
        val playersSubstitutedOut = previouslyAvailableById.filter { (id, prevPlayer) ->
            val currentPlayer = currentlyAvailableById[id]
            prevPlayer.status == RSVPStatus.AVAILABLE && currentPlayer?.status == RSVPStatus.UNAVAILABLE
        }.values
        
        // Find players whose status changed from UNAVAILABLE (or new) to AVAILABLE (substituted in)
        val playersSubstitutedIn = currentlyAvailableById.filter { (id, currentPlayer) ->
            val prevPlayer = previouslyAvailableById[id]
            currentPlayer.status == RSVPStatus.AVAILABLE && (prevPlayer == null || prevPlayer.status == RSVPStatus.UNAVAILABLE)
        }.values
        
        if (playersSubstitutedOut.isNotEmpty() || playersSubstitutedIn.isNotEmpty()) {
            Clogger.d("LineupFragment", "Handling substitution: ${playersSubstitutedOut.size} out, ${playersSubstitutedIn.size} in")
            
            val currentPositions = pitchView.getPositionedPlayers().toMutableMap()
            
            // Process each substitution
            playersSubstitutedOut.forEach { playerOut ->
                // Find where the player is positioned on the pitch
                val playerOutPosition = currentPositions.entries.find { it.value?.playerId == playerOut.playerId }?.key
                
                if (playerOutPosition != null) {
                    Clogger.d("LineupFragment", "Player ${playerOut.playerName} subbed out from position $playerOutPosition")
                    
                    // Find corresponding substitute (if any)
                    // For now, we'll just remove the player and let the system auto-assign if there's a substitute
                    // The substitution event should have been recorded, so we can match based on timing
                    // For simplicity, we'll take the first available substitute
                    val playerIn = playersSubstitutedIn.firstOrNull()
                    
                    if (playerIn != null) {
                        // Remove the old player from position (via ViewModel)
                        viewModel.removePlayerFromPosition(playerOutPosition)
                        
                        // Position the substitute in the same position (via ViewModel - this will persist and update UI)
                        viewModel.positionPlayer(playerIn, playerOutPosition)
                        
                        Clogger.d("LineupFragment", "Player ${playerIn.playerName} subbed in at position $playerOutPosition")
                        
                        // Show notification
                        Snackbar.make(requireView(), 
                            "Substitution: ${playerIn.playerName} ↔ ${playerOut.playerName}", 
                            Snackbar.LENGTH_LONG).show()
                    } else {
                        // No explicit substitute found from attendance deltas.
                        // Fallback: choose the first AVAILABLE bench player not on pitch and place into the same position.
                        val positionedIds = currentPositions.values.filterNotNull().map { it.playerId }.toSet()
                        val fallbackIn = availablePlayers.firstOrNull { it.status == RSVPStatus.AVAILABLE && it.playerId !in positionedIds }

                        if (fallbackIn != null) {
                            viewModel.removePlayerFromPosition(playerOutPosition)
                            viewModel.positionPlayer(fallbackIn, playerOutPosition)
                            Clogger.d("LineupFragment", "Fallback sub-in ${fallbackIn.playerName} at $playerOutPosition")
                            Snackbar.make(requireView(),
                                "Substitution: ${fallbackIn.playerName} ↔ ${playerOut.playerName}",
                                Snackbar.LENGTH_LONG).show()
                        } else {
                            // No substitute found, just remove the player from pitch (via ViewModel)
                            viewModel.removePlayerFromPosition(playerOutPosition)
                            Clogger.d("LineupFragment", "No substitute available, clearing position $playerOutPosition")
                            Snackbar.make(requireView(),
                                "${playerOut.playerName} substituted out",
                                Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            
            // Don't update pitch view directly - let the ViewModel observer handle it
        }
    }

    private fun updateAvailablePlayersGrid() {
        // Filter out players who are already positioned on the pitch
        val positionedPlayers = pitchView.getPositionedPlayers().values.filterNotNull()
        val positionedIds = positionedPlayers.map { it.playerId }.toSet()
        
        // Filter bench players: exclude those currently on pitch
        // Also exclude UNAVAILABLE players entirely (substituted players are removed from bench)
        val availableForBench = availablePlayers.filter { player ->
            val notOnPitch = !positionedIds.contains(player.playerId)
            val isAvailable = player.status == RSVPStatus.AVAILABLE
            notOnPitch && isAvailable
        }
        
        // Sort players alphabetically (all are AVAILABLE since we filtered UNAVAILABLE out)
        val sortedPlayers = availableForBench.sortedBy { it.playerName }
        
        // Update the grid adapter with sorted players
        playersAdapter.updatePlayers(sortedPlayers)
        
        // Update bench count display
        val activeCount = availablePlayers.count { it.status == RSVPStatus.AVAILABLE }
        val onPitch = positionedPlayers.size
        val availableOnBench = availableForBench.size
        
        val benchCountText = if (activeCount == 0) {
            "No players marked as Present in Attendance"
        } else {
            "Bench ($availableOnBench/${activeCount - onPitch})"
        }
        
        // Update the bench count text in the layout
        view?.findViewById<TextView>(R.id.squadCountText)?.text = benchCountText
    }

    private fun positionPlayer(player: RosterPlayer, position: String) {
        viewModel.positionPlayer(player, position)
        Snackbar.make(requireView(),
            "${player.playerName} positioned at $position",
            Snackbar.LENGTH_SHORT).show()
    }

    private fun removePlayerFromPosition(position: String) {
        viewModel.removePlayerFromPosition(position)
        Snackbar.make(requireView(),
            "Player removed from $position",
            Snackbar.LENGTH_SHORT).show()
    }

    private fun updatePlayerRole(position: String, player: RosterPlayer, role: String) {
        // TODO: Backend - Update player role
        Snackbar.make(requireView(), 
            "${player.playerName} role changed to $role", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun viewPlayerDetails(player: RosterPlayer) {
        // TODO: Navigate to player details screen
        Snackbar.make(requireView(), 
            "Opening ${player.playerName}'s details", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePlayerDrop(position: String, dropPoint: PointF) {
        // Check if this is a swap scenario (dropPoint.x == -1f indicates swap)
        if (dropPoint.x == -1f && dropPoint.y == -1f) {
            // This is a swap scenario - handle player swapping
            handlePlayerSwap(position)
        } else {
            // Regular drop - find the closest valid position to the drop point
            val closestPosition = findClosestPosition(dropPoint)
            
            if (closestPosition != null) {
                // Get the player being dragged from the drop event
                val draggedPlayer = pitchView.getPositionedPlayers()[position]
                
                if (draggedPlayer != null) {
                    // Player is already on the pitch - handle movement/swapping
                    handlePlayerMovement(position, closestPosition, draggedPlayer)
                } else {
                    // Player is being dropped from the grid - the FormationPitchView already handled it
                    // Just update the available players grid
                    updateAvailablePlayersGrid()
                    
                    // Show success message
                    val player = pitchView.getPositionedPlayers()[closestPosition]
                    player?.let {
                        Snackbar.make(requireView(), 
                            "${it.playerName} positioned at $closestPosition", 
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Invalid drop - snap back to original position
                Snackbar.make(requireView(), 
                    "Invalid position - player returned", 
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handlePlayerSwap(targetPosition: String) {
        // Get the current player at the target position
        val currentPositions = pitchView.getPositionedPlayers().toMutableMap()
        val targetPlayer = currentPositions[targetPosition]
        
        // Get the dragged player from the current dragged player state
        val draggedPlayer = currentDraggedPlayer
        
        if (targetPlayer != null && draggedPlayer != null) {
            // Find where the dragged player currently is on the pitch
            val draggedPlayerPosition = currentPositions.entries.find { it.value?.playerId == draggedPlayer.playerId }?.key
            
            if (draggedPlayerPosition != null) {
                // Persist swap
                viewModel.swapPlayers(draggedPlayerPosition, targetPosition)
                Snackbar.make(requireView(), 
                    "Players swapped: ${draggedPlayer.playerName} ↔ ${targetPlayer.playerName}", 
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // Clear the dragged player state
        currentDraggedPlayer = null
    }

    private fun handlePlayerMovement(fromPosition: String, toPosition: String, player: RosterPlayer) {
        val currentPositions = pitchView.getPositionedPlayers().toMutableMap()
        val targetPlayer = currentPositions[toPosition]
        
        if (targetPlayer != null) {
            viewModel.swapPlayers(fromPosition, toPosition)
            Snackbar.make(requireView(), 
                "Players swapped: ${player.playerName} ↔ ${targetPlayer.playerName}", 
                Snackbar.LENGTH_SHORT).show()
        } else {
            viewModel.removePlayerFromPosition(fromPosition)
            viewModel.positionPlayer(player, toPosition)
            Snackbar.make(requireView(), 
                "${player.playerName} moved to $toPosition", 
                Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun findClosestPosition(dropPoint: PointF): String? {
        val availablePositions = pitchView.getAvailablePositions()
        var closestPosition: String? = null
        var minDistance = Float.MAX_VALUE
        val maxSnapDistance = 80f // Maximum distance for snapping
        
        for (position in availablePositions) {
            val positionPoint = getPositionPoint(position)
            if (positionPoint != null) {
                val distance = kotlin.math.sqrt(
                    ((dropPoint.x - positionPoint.x) * (dropPoint.x - positionPoint.x) + 
                     (dropPoint.y - positionPoint.y) * (dropPoint.y - positionPoint.y)).toDouble()
                ).toFloat()
                
                if (distance < minDistance && distance <= maxSnapDistance) {
                    minDistance = distance
                    closestPosition = position
                }
            }
        }
        
        return closestPosition
    }

    private fun getPositionPoint(position: String): PointF? {
        return pitchView.getPositionCoordinates(position)
    }

    private fun showMatchEventsBottomSheet() {
        val bottomSheet = MatchEventsBottomSheet.newInstance()
        // Pass match ID to the bottom sheet
        bottomSheet.arguments = Bundle().apply {
            putString("event_id", matchId)
        }
        bottomSheet.show(parentFragmentManager, "MatchEventsBottomSheet")
    }
}
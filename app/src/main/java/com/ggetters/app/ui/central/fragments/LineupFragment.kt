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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.LineupPlayerGridAdapter
import com.ggetters.app.ui.central.models.PlayerAvailability
import com.ggetters.app.ui.central.models.RSVPStatus
import com.ggetters.app.ui.central.viewmodels.LineupViewModel
import com.ggetters.app.ui.central.views.components.FormationPitchView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement formation management
// TODO: Backend - Add player position assignments  
// TODO: Backend - Implement drag-and-drop player positioning
// TODO: Backend - Add formation templates and presets

@AndroidEntryPoint
class LineupFragment : Fragment() {

    private val viewModel: LineupViewModel by viewModels()
    
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
    
    // Adapters
    private lateinit var playersAdapter: LineupPlayerGridAdapter
    
    // Data
    private var availablePlayers = listOf<PlayerAvailability>()
    private var currentFormation = "4-3-3"
    private val formations = listOf("4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2")

    companion object {
        fun newInstance(matchId: String, matchTitle: String, homeTeam: String, awayTeam: String): LineupFragment {
            val fragment = LineupFragment()
            val args = Bundle().apply {
                putString("match_id", matchId)
                putString("match_title", matchTitle)
                putString("home_team", homeTeam)
                putString("away_team", awayTeam)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString("match_id", "")
            matchTitle = it.getString("match_title", "")
            homeTeam = it.getString("home_team", "")
            awayTeam = it.getString("away_team", "")
        }
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
        
        // Search button click
        searchButton.setOnClickListener {
            showPlayerSearch()
        }
        
        // Setup pitch interactions
        setupPitchInteractions()
    }

    private fun setupPitchInteractions() {
        // Player click - show player options
        pitchView.setOnPlayerClickListener { position, player ->
            player?.let { showPlayerOptionsDialog(position, it) }
        }
        
        // Empty position click - show player selection
        pitchView.setOnPositionClickListener { position ->
            showPlayerSelectionDialog(position)
        }
        
        // Player dropped - handle position change
        pitchView.setOnPlayerDroppedListener { position, dropPoint ->
            handlePlayerDrop(position, dropPoint)
        }
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
            onAddPlayerClick = { showAddPlayer() }
        )
        
        // Grid layout with 4 columns as shown in sketch
        val gridLayoutManager = GridLayoutManager(requireContext(), 4)
        playersRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = playersAdapter
        }
    }

    private fun loadPlayerData() {
        // TODO: Backend - Load actual player data
        // Create sample data matching available players
        availablePlayers = listOf(
            PlayerAvailability("1", "Aaron Robertson", "GK", 1, RSVPStatus.AVAILABLE),
            PlayerAvailability("2", "Jacob Holdford", "CB", 4, RSVPStatus.AVAILABLE),
            PlayerAvailability("3", "Matthew Mokotle", "CM", 8, RSVPStatus.AVAILABLE),
            PlayerAvailability("4", "Dylan Seedat", "FW", 9, RSVPStatus.AVAILABLE),
            PlayerAvailability("5", "Arjan Bidnugram", "CB", 5, RSVPStatus.AVAILABLE),
            PlayerAvailability("6", "Fortune Manthata", "ST", 10, RSVPStatus.AVAILABLE),
            PlayerAvailability("7", "Luke Jackson", "ST", 11, RSVPStatus.AVAILABLE),
            PlayerAvailability("8", "Mike Wilson", "CM", 6, RSVPStatus.AVAILABLE),
            PlayerAvailability("9", "David Smith", "FW", 7, RSVPStatus.AVAILABLE),
            PlayerAvailability("10", "Chris Brown", "LB", 3, RSVPStatus.AVAILABLE),
            PlayerAvailability("11", "Tom Davis", "RB", 2, RSVPStatus.AVAILABLE),
            PlayerAvailability("12", "Alex Miller", "CM", 12, RSVPStatus.AVAILABLE),
            PlayerAvailability("13", "Sam Wilson", "SUB", 13, RSVPStatus.AVAILABLE),
            PlayerAvailability("14", "Jake Taylor", "SUB", 14, RSVPStatus.AVAILABLE),
            PlayerAvailability("15", "Ben Moore", "SUB", 15, RSVPStatus.AVAILABLE),
            PlayerAvailability("16", "Ryan White", "SUB", 16, RSVPStatus.AVAILABLE),
            PlayerAvailability("17", "Mark Lewis", "SUB", 17, RSVPStatus.AVAILABLE),
            PlayerAvailability("18", "Paul Clark", "SUB", 18, RSVPStatus.AVAILABLE),
            PlayerAvailability("19", "Steve Hall", "SUB", 19, RSVPStatus.AVAILABLE),
            PlayerAvailability("20", "Nick Allen", "SUB", 20, RSVPStatus.AVAILABLE)
        )
        
        // Update players grid
        playersAdapter.updatePlayers(availablePlayers)
        
        // Set initial formation on pitch
        updatePitchFormation()
    }

    private fun changeFormation(newFormation: String) {
        currentFormation = newFormation
        updatePitchFormation()
        
        Snackbar.make(requireView(), 
            "Formation changed to $newFormation", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun updatePitchFormation() {
        // TODO: Backend - Load actual lineup data
        // Set formation and update pitch view
        pitchView.setFormation(currentFormation)
        
        // Add sample positioned players for 4-3-3 formation
        when (currentFormation) {
            "4-3-3" -> setup433Formation()
            "4-4-2" -> setup442Formation()
            "3-5-2" -> setup352Formation()
            "4-2-3-1" -> setup4231Formation()
            "5-3-2" -> setup532Formation()
        }
    }

    private fun setup433Formation() {
        // Sample players positioned in 4-3-3 formation
        val formationPlayers = mapOf(
            "GK" to availablePlayers.find { it.position == "GK" },
            "LB" to availablePlayers.find { it.position == "LB" },
            "CB1" to availablePlayers.find { it.position == "CB" },
            "CB2" to availablePlayers.getOrNull(1),
            "RB" to availablePlayers.find { it.position == "RB" },
            "CM1" to availablePlayers.find { it.position == "CM" },
            "CM2" to availablePlayers.filter { it.position == "CM" }.getOrNull(1),
            "CM3" to availablePlayers.filter { it.position == "CM" }.getOrNull(2),
            "LW" to availablePlayers.find { it.position == "FW" },
            "ST" to availablePlayers.find { it.position == "ST" },
            "RW" to availablePlayers.filter { it.position == "FW" }.getOrNull(1)
        )
        
        pitchView.setPlayers(formationPlayers)
    }

    private fun setup442Formation() {
        // TODO: Implement 4-4-2 formation setup
        pitchView.setPlayers(emptyMap())
    }

    private fun setup352Formation() {
        // TODO: Implement 3-5-2 formation setup
        pitchView.setPlayers(emptyMap())
    }

    private fun setup4231Formation() {
        // TODO: Implement 4-2-3-1 formation setup
        pitchView.setPlayers(emptyMap())
    }

    private fun setup532Formation() {
        // TODO: Implement 5-3-2 formation setup
        pitchView.setPlayers(emptyMap())
    }

    private fun handlePlayerSelection(player: PlayerAvailability) {
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
        // TODO: Observe lineup data changes
        // viewModel.lineup.observe(viewLifecycleOwner) { lineup ->
        //     updatePitchFormation()
        // }
    }

    // Public method to get current formation
    fun getCurrentFormation(): String = currentFormation

    // Public method to get positioned players
    fun getPositionedPlayers(): Map<String, PlayerAvailability?> {
        return pitchView.getPositionedPlayers()
    }

    // Dialog methods for player management
    private fun showPlayerOptionsDialog(position: String, player: PlayerAvailability) {
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

    private fun showRoleSelectionDialog(position: String, player: PlayerAvailability) {
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

    private fun positionPlayer(player: PlayerAvailability, position: String) {
        // TODO: Backend - Update player position
        val currentPositions = pitchView.getPositionedPlayers().toMutableMap()
        currentPositions[position] = player
        pitchView.setPlayers(currentPositions)
        
        Snackbar.make(requireView(), 
            "${player.playerName} positioned at $position", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun removePlayerFromPosition(position: String) {
        // TODO: Backend - Remove player from position
        val currentPositions = pitchView.getPositionedPlayers().toMutableMap()
        currentPositions.remove(position)
        pitchView.setPlayers(currentPositions)
        
        Snackbar.make(requireView(), 
            "Player removed from $position", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun updatePlayerRole(position: String, player: PlayerAvailability, role: String) {
        // TODO: Backend - Update player role
        Snackbar.make(requireView(), 
            "${player.playerName} role changed to $role", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun viewPlayerDetails(player: PlayerAvailability) {
        // TODO: Navigate to player details screen
        Snackbar.make(requireView(), 
            "Opening ${player.playerName}'s details", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePlayerDrop(position: String, dropPoint: PointF) {
        // TODO: Handle drag and drop repositioning
        // For now, just validate the drop
        if (pitchView.isValidDropPosition(position, dropPoint)) {
            Snackbar.make(requireView(), 
                "Player repositioned", 
                Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), 
                "Invalid position", 
                Snackbar.LENGTH_SHORT).show()
        }
    }
}
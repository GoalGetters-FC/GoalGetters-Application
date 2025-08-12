package com.ggetters.app.ui.central.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.AttendancePlayerAdapter
import com.ggetters.app.ui.central.models.PlayerAvailability
import com.ggetters.app.ui.central.models.RSVPStatus
import com.ggetters.app.ui.central.viewmodels.AttendanceViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement real-time player availability updates
// TODO: Backend - Add push notifications for availability changes
// TODO: Backend - Implement coach permission checks for player management
// TODO: Backend - Add bulk attendance operations

@AndroidEntryPoint
class AttendanceFragment : Fragment() {

    private val viewModel: AttendanceViewModel by viewModels()
    
    // Arguments
    private var matchId: String = ""
    private var matchTitle: String = ""
    
    // UI Components
    private lateinit var refereesRecyclerView: RecyclerView
    private lateinit var substitutesRecyclerView: RecyclerView
    private lateinit var unknownRecyclerView: RecyclerView
    
    // Adapters
    private lateinit var refereesAdapter: AttendancePlayerAdapter
    private lateinit var substitutesAdapter: AttendancePlayerAdapter
    private lateinit var unknownAdapter: AttendancePlayerAdapter
    
    // Data
    private var allPlayers = listOf<PlayerAvailability>()

    companion object {
        fun newInstance(matchId: String, matchTitle: String): AttendanceFragment {
            val fragment = AttendanceFragment()
            val args = Bundle().apply {
                putString("match_id", matchId)
                putString("match_title", matchTitle)
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews(view)
        loadPlayerData()
        observeViewModel()
    }

    private fun setupRecyclerViews(view: View) {
        // Referees section
        refereesRecyclerView = view.findViewById(R.id.refereesRecyclerView)
        refereesAdapter = AttendancePlayerAdapter { player, action ->
            handlePlayerAction(player, action)
        }
        refereesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = refereesAdapter
            isNestedScrollingEnabled = false
        }
        
        // Substitutes section
        substitutesRecyclerView = view.findViewById(R.id.substitutesRecyclerView)
        substitutesAdapter = AttendancePlayerAdapter { player, action ->
            handlePlayerAction(player, action)
        }
        substitutesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = substitutesAdapter
            isNestedScrollingEnabled = false
        }
        
        // Unknown section
        unknownRecyclerView = view.findViewById(R.id.unknownRecyclerView)
        unknownAdapter = AttendancePlayerAdapter { player, action ->
            handlePlayerAction(player, action)
        }
        unknownRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = unknownAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadPlayerData() {
        // TODO: Backend - Load actual player data from repository
        // Create sample data matching the sketch
        allPlayers = listOf(
            // Referees section (Available players)
            PlayerAvailability("1", "Aaron Robertson", "GK", 1, RSVPStatus.AVAILABLE),
            PlayerAvailability("2", "Jacob Holdford", "CB", 4, RSVPStatus.AVAILABLE),
            PlayerAvailability("3", "Matthew Mokotle", "CM", 8, RSVPStatus.AVAILABLE),
            
            // Substitutes section (Maybe/Available reserves)
            PlayerAvailability("4", "Dylan Seedat", "SUB", 12, RSVPStatus.MAYBE),
            PlayerAvailability("5", "Arjan Bidnugram", "SUB", 13, RSVPStatus.MAYBE),
            
            // Unknown section (Not responded)
            PlayerAvailability("6", "Fortune Manthata", "ST", 9, RSVPStatus.NOT_RESPONDED)
        )
        
        updatePlayerLists()
    }

    private fun updatePlayerLists() {
        // Group players by availability status
        val referees = allPlayers.filter { it.status == RSVPStatus.AVAILABLE }
        val substitutes = allPlayers.filter { it.status == RSVPStatus.MAYBE }
        val unknown = allPlayers.filter { it.status == RSVPStatus.NOT_RESPONDED }
        
        // Update adapters
        refereesAdapter.updatePlayers(referees)
        substitutesAdapter.updatePlayers(substitutes)
        unknownAdapter.updatePlayers(unknown)
    }

    private fun handlePlayerAction(player: PlayerAvailability, action: String) {
        when (action) {
            "menu" -> showPlayerMenu(player)
            "status_change" -> changePlayerStatus(player)
        }
    }

    private fun showPlayerMenu(player: PlayerAvailability) {
        // Find the view for this player to anchor the popup
        val view = requireView()
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_player_attendance, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_set_available -> {
                    updatePlayerStatus(player, RSVPStatus.AVAILABLE)
                    true
                }
                R.id.action_set_maybe -> {
                    updatePlayerStatus(player, RSVPStatus.MAYBE)
                    true
                }
                R.id.action_set_unavailable -> {
                    updatePlayerStatus(player, RSVPStatus.UNAVAILABLE)
                    true
                }
                R.id.action_set_not_responded -> {
                    updatePlayerStatus(player, RSVPStatus.NOT_RESPONDED)
                    true
                }
                R.id.action_send_reminder -> {
                    sendReminder(player)
                    true
                }
                R.id.action_view_profile -> {
                    viewPlayerProfile(player)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }

    private fun changePlayerStatus(player: PlayerAvailability) {
        // Cycle through statuses: Available -> Maybe -> Not Responded -> Available
        val newStatus = when (player.status) {
            RSVPStatus.AVAILABLE -> RSVPStatus.MAYBE
            RSVPStatus.MAYBE -> RSVPStatus.NOT_RESPONDED
            RSVPStatus.NOT_RESPONDED -> RSVPStatus.AVAILABLE
            RSVPStatus.UNAVAILABLE -> RSVPStatus.AVAILABLE
        }
        
        updatePlayerStatus(player, newStatus)
    }

    private fun updatePlayerStatus(player: PlayerAvailability, newStatus: RSVPStatus) {
        // TODO: Backend - Update player status in backend
        val updatedPlayers = allPlayers.map { 
            if (it.playerId == player.playerId) {
                it.copy(status = newStatus)
            } else {
                it
            }
        }
        
        allPlayers = updatedPlayers
        updatePlayerLists()
        
        val statusText = when (newStatus) {
            RSVPStatus.AVAILABLE -> "Available"
            RSVPStatus.MAYBE -> "Maybe"
            RSVPStatus.UNAVAILABLE -> "Unavailable"
            RSVPStatus.NOT_RESPONDED -> "Not Responded"
        }
        
        Snackbar.make(requireView(), 
            "${player.playerName} marked as $statusText", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun sendReminder(player: PlayerAvailability) {
        // TODO: Backend - Send reminder notification to player
        Snackbar.make(requireView(), 
            "Reminder sent to ${player.playerName}", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun viewPlayerProfile(player: PlayerAvailability) {
        // TODO: Navigate to player profile
        Snackbar.make(requireView(), 
            "Opening ${player.playerName}'s profile", 
            Snackbar.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        // TODO: Observe player data changes
        // viewModel.players.observe(viewLifecycleOwner) { players ->
        //     allPlayers = players
        //     updatePlayerLists()
        // }
    }

    // Public method to refresh data
    fun refreshAttendance() {
        loadPlayerData()
    }

    // Public method to get attendance summary
    fun getAttendanceSummary(): Map<String, Int> {
        return mapOf(
            "available" to allPlayers.count { it.status == RSVPStatus.AVAILABLE },
            "maybe" to allPlayers.count { it.status == RSVPStatus.MAYBE },
            "unavailable" to allPlayers.count { it.status == RSVPStatus.UNAVAILABLE },
            "not_responded" to allPlayers.count { it.status == RSVPStatus.NOT_RESPONDED }
        )
    }
}
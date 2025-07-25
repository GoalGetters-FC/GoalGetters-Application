package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.models.PlayerStats
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.widget.ImageView
import com.google.android.material.button.MaterialButton


class HomePlayersFragment : Fragment() {
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var summaryChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup
    // Simulate user role for demo ("coach", "assistant", "player", "guardian")
    private val userRole = "coach"
    private var allPlayers: List<Player> = emptyList()
    private var filteredPlayers: List<Player> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_players, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        loadPlayers()
        setupFilterChips()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.playersRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        summaryChipGroup = view.findViewById(R.id.summaryChipGroup)
        filterChipGroup = view.findViewById(R.id.filterChipGroup)
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onPlayerClick = { player ->
                // TODO: Navigate to player profile/details screen
            },
            onPlayerLongClick = { player ->
                // TODO: Show player actions bottom sheet/modal (admin actions: edit role, remove, message, etc. - call backend as needed)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = playerAdapter
    }

    private fun loadPlayers() {
        // TODO: Fetch players from backend (replace sample data)
        // val players = playerRepo.getPlayersForTeam(teamId)
        allPlayers = listOf(
            Player(
                id = "1",
                name = "John Doe",
                position = "Forward",
                jerseyNumber = "10",
                avatar = null,
                isActive = true,
                stats = PlayerStats(goals = 15, assists = 8, matches = 25)
            ),
            Player(
                id = "2",
                name = "Jane Smith",
                position = "Midfielder",
                jerseyNumber = "8",
                avatar = null,
                isActive = true,
                stats = PlayerStats(goals = 5, assists = 12, matches = 22)
            ),
            Player(
                id = "3",
                name = "Mike Johnson",
                position = "Defender",
                jerseyNumber = "4",
                avatar = null,
                isActive = false,
                stats = PlayerStats(goals = 1, assists = 3, matches = 18)
            ),
            Player(
                id = "4",
                name = "Coach Smith",
                position = "Coach",
                jerseyNumber = "-",
                avatar = null,
                isActive = true,
                stats = PlayerStats(goals = 0, assists = 0, matches = 0)
            ),
            Player(
                id = "5",
                name = "Alex Lee",
                position = "Goalkeeper",
                jerseyNumber = "1",
                avatar = null,
                isActive = true,
                stats = PlayerStats(goals = 0, assists = 1, matches = 20)
            )
        )
        filteredPlayers = allPlayers
        updateSummaryChips()
        playerAdapter.updatePlayers(filteredPlayers)
        updateEmptyState(filteredPlayers.isEmpty())
    }

    private fun updateSummaryChips() {
        val playersCount = allPlayers.count { it.position != "Coach" && it.position != "Assistant" }
        val assistantsCount = allPlayers.count { it.position == "Assistant" }
        val coachCount = allPlayers.count { it.position == "Coach" }
        summaryChipGroup.findViewById<Chip>(R.id.chipPlayersCount).text = "Players: $playersCount"
        summaryChipGroup.findViewById<Chip>(R.id.chipAssistantsCount).text = "Assistants: $assistantsCount"
        summaryChipGroup.findViewById<Chip>(R.id.chipCoachCount).text = "Coach: $coachCount"
    }

    private fun setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            filteredPlayers = when (checkedId) {
                R.id.chipFilterAll -> allPlayers
                R.id.chipFilterPlayers -> allPlayers.filter { it.position != "Coach" && it.position != "Assistant" }
                R.id.chipFilterCoaches -> allPlayers.filter { it.position == "Coach" }
                R.id.chipFilterGoalkeepers -> allPlayers.filter { it.position.equals("Goalkeeper", true) }
                R.id.chipFilterNew -> allPlayers.filter { it.position.equals("New", true) }
                else -> allPlayers
            }
            playerAdapter.updatePlayers(filteredPlayers)
            updateEmptyState(filteredPlayers.isEmpty())
        }
        // Default selection
        filterChipGroup.check(R.id.chipFilterAll)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }
} 
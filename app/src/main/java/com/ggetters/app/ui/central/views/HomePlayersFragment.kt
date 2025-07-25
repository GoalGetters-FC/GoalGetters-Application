package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.models.PlayerStats
import com.ggetters.app.ui.central.viewmodels.HomePlayersViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePlayersFragment : Fragment() {


    private val activeModel: HomePlayersViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()


    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var summaryChipGroup: ChipGroup

    // Simulate user role for demo ("coach", "assistant", "player", "guardian")
    private val userRole = "coach"
    private var allPlayers: List<Player> = emptyList()
    private var filteredPlayers: List<Player> = emptyList()
    private var selectedFilter: String = "All"

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
        setHasOptionsMenu(true)
        setupRecyclerView()
        loadPlayers()
        // Remove setupFilterChips and all chipFilterXXX references
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_players_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.playersRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        summaryChipGroup = view.findViewById(R.id.summaryChipGroup)
    }

    private fun showFilterDialog() {
        val filters = arrayOf("All", "Players", "Coaches", "Goalkeepers", "New members")
        val checkedItem = filters.indexOf(selectedFilter).coerceAtLeast(0)
        AlertDialog.Builder(requireContext())
            .setTitle("Filter Players")
            .setSingleChoiceItems(filters, checkedItem) { dialog, which ->
                selectedFilter = filters[which]
            }
            .setPositiveButton("Apply") { dialog, _ ->
                applyFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyFilter() {
        // TODO: Use backend filtering if available
        filteredPlayers = when (selectedFilter) {
            "All" -> allPlayers
            "Players" -> allPlayers.filter { it.position != "Coach" && it.position != "Assistant" }
            "Coaches" -> allPlayers.filter { it.position == "Coach" }
            "Goalkeepers" -> allPlayers.filter { it.position.equals("Goalkeeper", true) }
            "New members" -> allPlayers.filter { it.position.equals("New", true) }
            else -> allPlayers
        }
        playerAdapter.updatePlayers(filteredPlayers)
        updateEmptyState(filteredPlayers.isEmpty())
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
        summaryChipGroup.findViewById<Chip>(R.id.chipAssistantsCount).text =
            "Assistants: $assistantsCount"
        summaryChipGroup.findViewById<Chip>(R.id.chipCoachCount).text = "Coach: $coachCount"
    }

    // Remove setupFilterChips and all chipFilterXXX references

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
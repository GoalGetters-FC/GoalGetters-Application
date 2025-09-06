// app/src/main/java/com/ggetters/app/ui/central/fragments/LineupFragment.kt
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.ui.central.adapters.LineupPlayerGridAdapter
import com.ggetters.app.ui.central.sheets.MatchEventsBottomSheet
import com.ggetters.app.ui.central.viewmodels.LineupViewModel
import com.ggetters.app.ui.central.views.components.FormationPitchView
import com.ggetters.app.ui.shared.extensions.getFullName
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LineupFragment : Fragment() {

    private val viewModel: LineupViewModel by viewModels()

    private var eventId: String = ""
    private var matchTitle: String = ""
    private var homeTeam: String = ""
    private var awayTeam: String = ""

    private lateinit var formationSpinner: Spinner
    private lateinit var searchButton: ImageButton
    private lateinit var pitchView: FormationPitchView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var fabMatchEvents: FloatingActionButton

    private lateinit var playersAdapter: LineupPlayerGridAdapter
    private var availablePlayers: List<RosterPlayer> = emptyList()
    private val formations = listOf("4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2")

    companion object {
        fun newInstance(eventId: String): LineupFragment =
            LineupFragment().apply {
                arguments = Bundle().apply { putString("event_id", eventId) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString("event_id", it.getString("match_id", "")) ?: ""
            matchTitle = it.getString("match_title", "")
            homeTeam = it.getString("home_team", "")
            awayTeam = it.getString("away_team", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_lineup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupFormationSpinner()
        setupPlayersGrid()
        setupPitchInteractions()
        observeViewModel()

        if (eventId.isBlank()) {
            Snackbar.make(requireView(), "Missing event id", Snackbar.LENGTH_LONG).show()
        } else {
            viewModel.loadLineup(eventId)
        }
    }

    private fun setupViews(view: View) {
        formationSpinner = view.findViewById(R.id.formationSpinner)
        searchButton = view.findViewById(R.id.searchButton)
        pitchView = view.findViewById(R.id.pitchView)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)
        fabMatchEvents = view.findViewById(R.id.fabMatchEvents)

        searchButton.setOnClickListener { showPlayerSearch() }
        fabMatchEvents.setOnClickListener { showMatchEventsBottomSheet() }
    }

    private fun setupFormationSpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.item_formation_spinner, formations)
        adapter.setDropDownViewResource(R.layout.item_formation_spinner_dropdown)
        formationSpinner.adapter = adapter

        formationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                viewModel.updateFormation(formations[pos])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupPlayersGrid() {
        playersAdapter = LineupPlayerGridAdapter(
            onPlayerClick = { player -> handlePlayerSelection(player) },
            onAddPlayerClick = { showAddPlayer() },
            onPlayerDragStart = { /* optionally track drag state */ }
        )
        playersRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        playersRecyclerView.adapter = playersAdapter
    }

    private fun setupPitchInteractions() {
        pitchView.setOnPlayerDroppedListener { sourcePosition, dropPoint ->
            handlePlayerDrop(sourcePosition, dropPoint)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.players.collect { list ->
                        availablePlayers = list
                        updateAvailablePlayersGrid()
                    }
                }
                launch {
                    viewModel.formation.collect { f ->
                        val idx = formations.indexOf(f).coerceAtLeast(0)
                        if (idx != formationSpinner.selectedItemPosition) {
                            formationSpinner.setSelection(idx, false)
                        }
                        pitchView.setFormation(f)
                    }
                }
                launch {
                    viewModel.positionedPlayers.collect { map ->
                        pitchView.setPlayers(map)
                        updateAvailablePlayersGrid()
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateAvailablePlayersGrid() {
        val onPitch = viewModel.positionedPlayers.value.values.filterNotNull().toSet()
        val bench = availablePlayers.filter { p -> onPitch.none { it.playerId == p.playerId } }
        playersAdapter.updatePlayers(bench)
    }

    private fun handlePlayerSelection(player: RosterPlayer) {
        val availablePositions = pitchView.getAvailablePositions().filterNot { pitchView.isPositionOccupied(it) }
        if (availablePositions.isEmpty()) {
            Snackbar.make(requireView(), "All positions are occupied", Snackbar.LENGTH_SHORT).show()
            return
        }
        val names = availablePositions.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Position ${player.playerName}")
            .setItems(names) { _, which -> viewModel.positionPlayer(player, names[which]) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddPlayer() {
        // Multi-select of team users not yet on the event roster
        val candidates = viewModel.getAddableUsers()
        if (candidates.isEmpty()) {
            Snackbar.make(requireView(), "All team users already in this event", Snackbar.LENGTH_SHORT).show()
            return
        }
        val labels = candidates.map { it.getFullName() }.toTypedArray()
        val checked = BooleanArray(candidates.size)
        val chosen = mutableListOf<Int>()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add players to event")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                if (isChecked) chosen.add(which) else chosen.remove(which)
            }
            .setPositiveButton("Add") { _, _ ->
                val ids = chosen.map { candidates[it].id }
                viewModel.addPlayersToEvent(ids)
                Snackbar.make(requireView(), "Added ${ids.size} player(s) to event roster", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlayerSearch() {
        Snackbar.make(requireView(), "Player search coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePlayerDrop(sourcePosition: String, dropPoint: PointF) {
        val target = findClosestPosition(dropPoint) ?: run {
            Snackbar.make(requireView(), "Invalid position - player returned", Snackbar.LENGTH_SHORT).show()
            return
        }
        val current = viewModel.positionedPlayers.value
        val dragged = current[sourcePosition]
        val occupant = current[target]

        when {
            dragged == null -> {
                // Likely a bench → pitch drop already handled by the pitch view
                updateAvailablePlayersGrid()
            }
            occupant == null -> {
                viewModel.removePlayerFromPosition(sourcePosition)
                viewModel.positionPlayer(dragged, target)
                Snackbar.make(requireView(), "${dragged.playerName} moved to $target", Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                viewModel.swapPlayers(sourcePosition, target)
                Snackbar.make(requireView(), "Players swapped", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun findClosestPosition(dropPoint: PointF): String? {
        val available = pitchView.getAvailablePositions()
        var closest: String? = null
        var min = Float.MAX_VALUE
        val snap = 80f
        for (pos in available) {
            val pt = pitchView.getPositionCoordinates(pos) ?: continue
            val dx = dropPoint.x - pt.x
            val dy = dropPoint.y - pt.y
            val d = kotlin.math.sqrt(dx*dx + dy*dy)
            if (d < min && d <= snap) { min = d; closest = pos }
        }
        return closest
    }

    private fun showMatchEventsBottomSheet() {
        MatchEventsBottomSheet.newInstance().show(parentFragmentManager, "MatchEventsBottomSheet")
    }
}

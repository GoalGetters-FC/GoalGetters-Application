package com.ggetters.app.ui.central.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.AttendanceWithUser
import com.ggetters.app.ui.central.adapters.AttendancePlayerAdapter
import com.ggetters.app.ui.central.viewmodels.AttendanceViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AttendanceFragment : Fragment() {

    private val viewModel: AttendanceViewModel by viewModels()

    private var matchId: String = ""
    private lateinit var refereesAdapter: AttendancePlayerAdapter
    private lateinit var substitutesAdapter: AttendancePlayerAdapter
    private lateinit var unknownAdapter: AttendancePlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = arguments?.getString("match_id", "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_attendance, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews(view)
        observeViewModel()
        viewModel.loadPlayers(matchId)
    }

    private fun setupRecyclerViews(view: View) {
        refereesAdapter = AttendancePlayerAdapter { player, action -> handlePlayerAction(player, action) }
        substitutesAdapter = AttendancePlayerAdapter { player, action -> handlePlayerAction(player, action) }
        unknownAdapter = AttendancePlayerAdapter { player, action -> handlePlayerAction(player, action) }

        view.findViewById<RecyclerView>(R.id.refereesRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = refereesAdapter
        }
        view.findViewById<RecyclerView>(R.id.substitutesRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = substitutesAdapter
        }
        view.findViewById<RecyclerView>(R.id.unknownRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = unknownAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.players.collectLatest { players ->
                val present = players.filter { it.attendance.status == 0 }
                val absent = players.filter { it.attendance.status == 1 }
                val late = players.filter { it.attendance.status == 2 }
                val excused = players.filter { it.attendance.status == 3 }

                refereesAdapter.updatePlayers(present)
                substitutesAdapter.updatePlayers(late)
                unknownAdapter.updatePlayers(absent + excused)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest { err ->
                err?.let {
                    Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun handlePlayerAction(player: AttendanceWithUser, action: String) {
        when (action) {
            "menu" -> showPlayerMenu(player)
            "status_change" -> cyclePlayerStatus(player)
        }
    }

    private fun showPlayerMenu(player: AttendanceWithUser) {
        val popup = PopupMenu(requireContext(), requireView())
        popup.menuInflater.inflate(R.menu.menu_player_attendance, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            val newStatus = when (menuItem.itemId) {
                R.id.action_set_available -> 0
                R.id.action_set_unavailable -> 1
                R.id.action_set_maybe -> 2
                R.id.action_set_not_responded -> 3
                else -> null
            }
            newStatus?.let {
                viewModel.updatePlayerStatus(player.attendance.eventId, player.user.id, it)
            }
            true
        }
        popup.show()
    }

    private fun cyclePlayerStatus(player: AttendanceWithUser) {
        val current = player.attendance.status
        val newStatus = (current + 1) % 4
        viewModel.updatePlayerStatus(player.attendance.eventId, player.user.id, newStatus)
    }

    companion object {
        fun newInstance(eventId: String): AttendanceFragment {
            val fragment = AttendanceFragment()
            fragment.arguments = Bundle().apply {
                putString("event_id", eventId)
            }
            return fragment
        }
    }

}

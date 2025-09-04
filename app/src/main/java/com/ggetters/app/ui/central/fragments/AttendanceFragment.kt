package com.ggetters.app.ui.central.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
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
    private lateinit var presentAdapter: AttendancePlayerAdapter
    private lateinit var absentAdapter: AttendancePlayerAdapter
    private lateinit var lateAdapter: AttendancePlayerAdapter
    private lateinit var excusedAdapter: AttendancePlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔴 Fix: use consistent key "event_id"
        matchId = arguments?.getString("event_id", "") ?: ""
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
        presentAdapter = AttendancePlayerAdapter { player, action, clickedView -> handlePlayerAction(player, action, clickedView) }
        absentAdapter = AttendancePlayerAdapter { player, action, clickedView -> handlePlayerAction(player, action, clickedView) }
        lateAdapter = AttendancePlayerAdapter { player, action, clickedView -> handlePlayerAction(player, action, clickedView) }
        excusedAdapter = AttendancePlayerAdapter { player, action, clickedView -> handlePlayerAction(player, action, clickedView) }

        view.findViewById<RecyclerView>(R.id.presentRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = presentAdapter
        }
        view.findViewById<RecyclerView>(R.id.absentRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = absentAdapter
        }
        view.findViewById<RecyclerView>(R.id.lateRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lateAdapter
        }
        view.findViewById<RecyclerView>(R.id.excusedRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = excusedAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.players.collectLatest { players ->
                // Fixed: Proper status mapping - 0=Present,1=Absent,2=Late,3=Excused
                val present = players.filter { it.attendance.status == 0 }
                val absent = players.filter { it.attendance.status == 1 }
                val late = players.filter { it.attendance.status == 2 }
                val excused = players.filter { it.attendance.status == 3 }

                presentAdapter.updatePlayers(present)
                absentAdapter.updatePlayers(absent)
                lateAdapter.updatePlayers(late)
                excusedAdapter.updatePlayers(excused)
                
                updateSectionCounts(present.size, absent.size, late.size, excused.size)
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

    private fun updateSectionCounts(presentCount: Int, absentCount: Int, lateCount: Int, excusedCount: Int) {
        view?.let { view ->
            view.findViewById<TextView>(R.id.presentSectionTitle)?.text = "Present ($presentCount)"
            view.findViewById<TextView>(R.id.absentSectionTitle)?.text = "Absent ($absentCount)"
            view.findViewById<TextView>(R.id.lateSectionTitle)?.text = "Late ($lateCount)"
            view.findViewById<TextView>(R.id.excusedSectionTitle)?.text = "Excused ($excusedCount)"
        }
    }

    private fun handlePlayerAction(player: AttendanceWithUser, action: String, clickedView: View?) {
        when (action) {
            "menu" -> showPlayerMenu(player, clickedView)
            "status_change" -> cyclePlayerStatus(player)
        }
    }

    private fun showPlayerMenu(player: AttendanceWithUser, anchor: View?) {
        // Use the clicked button as anchor, or fall back to the fragment view
        val anchorView = anchor ?: requireView()
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_player_attendance, popup.menu)
        
        // Force popup to show below the anchor for better positioning
        try {
            val field = PopupMenu::class.java.getDeclaredField("mPopup")
            field.isAccessible = true
            val menuPopupHelper = field.get(popup)
            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
            val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
            setForceIcons.invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            // Ignore if reflection fails - menu will still work without icons
        }

        popup.setOnMenuItemClickListener { menuItem ->
            val newStatus = when (menuItem.itemId) {
                R.id.action_set_available -> 0      // Present
                R.id.action_set_unavailable -> 1    // Absent  
                R.id.action_set_maybe -> 2          // Late
                R.id.action_set_not_responded -> 3  // Excused
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

package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.ui.central.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    companion object {
        private const val ARG_PLAYER_ID = "player_id"

        fun newInstance(playerId: String): StatisticsFragment {
            return StatisticsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PLAYER_ID, playerId)
                }
            }
        }
    }

    private val viewModel: StatisticsViewModel by viewModels()

    private var playerId: String? = null
    private var currentPlayer: User? = null

    // Header
    private lateinit var closeButton: ImageButton
    private lateinit var titleText: TextView

    // Donut chart section
    private lateinit var scheduledCount: TextView
    private lateinit var attendedCount: TextView
    private lateinit var missedCount: TextView

    // Statistics cards
    private lateinit var goalsValue: TextView
    private lateinit var assistsValue: TextView
    private lateinit var matchesValue: TextView
    private lateinit var yellowCardsValue: TextView
    private lateinit var weightValue: TextView
    private lateinit var minutesValue: TextView

    private fun setupStatusBar() {
        // Hide the system status bar to use our custom header
        requireActivity().window.statusBarColor = android.graphics.Color.parseColor("#161620")
        
        // Set up window insets controller for dark status bar
        val windowInsetsController = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // Dark status bar icons for dark background
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerId = it.getString(ARG_PLAYER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_statistics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        setupViews(view)
        setupActions()

        // Load player data
        playerId?.let { id ->
            viewModel.loadPlayerStatistics(id)
            viewModel.observePlayerStatistics(id) // Start observing real-time updates
        }

        viewModel.player.observe(viewLifecycleOwner, Observer { player ->
            player?.let {
                currentPlayer = it
                displayPlayerInfo(it)
            }
        })

        viewModel.statistics.observe(viewLifecycleOwner, Observer { stats ->
            stats?.let {
                displayStatistics(it)
            }
        })
    }

    private fun setupViews(view: View) {
        // Header
        closeButton = view.findViewById(R.id.closeButton)
        titleText = view.findViewById(R.id.titleText)

        // Donut chart section
        scheduledCount = view.findViewById(R.id.scheduledCount)
        attendedCount = view.findViewById(R.id.attendedCount)
        missedCount = view.findViewById(R.id.missedCount)

        // Statistics cards
        goalsValue = view.findViewById(R.id.goalsValue)
        assistsValue = view.findViewById(R.id.assistsValue)
        matchesValue = view.findViewById(R.id.matchesValue)
        yellowCardsValue = view.findViewById(R.id.yellowCardsValue)
        weightValue = view.findViewById(R.id.weightValue)
        minutesValue = view.findViewById(R.id.minutesValue)
    }

    private fun setupActions() {
        // Close button - navigate back
        closeButton.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun displayPlayerInfo(player: User) {
        titleText.text = "${player.fullName()} Statistics"
    }

    private fun displayStatistics(stats: com.ggetters.app.data.model.PlayerStatistics) {
        // Attendance donut chart data
        scheduledCount.text = stats.scheduled.toString()
        attendedCount.text = stats.attended.toString()
        missedCount.text = stats.missed.toString()

        // Performance statistics
        goalsValue.text = stats.goals.toString()
        assistsValue.text = stats.assists.toString()
        matchesValue.text = stats.matches.toString()
        yellowCardsValue.text = stats.yellowCards.toString()
        weightValue.text = "${stats.weight.toInt()} kg"
        minutesValue.text = "${stats.minutesPlayed} min"
    }
}

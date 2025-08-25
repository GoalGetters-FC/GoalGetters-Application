package com.ggetters.app.ui.central.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.MatchEventAdapter
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.viewmodels.MatchDetailsViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

// TODO: Backend - Implement real-time match data loading
// TODO: Backend - Add live score updates during match
// TODO: Backend - Implement match status synchronization
// TODO: Backend - Add error handling and retry mechanisms

@AndroidEntryPoint
class MatchDetailsFragment : Fragment() {

    private val viewModel: MatchDetailsViewModel by viewModels()
    
    // Arguments
    private var matchId: String = ""
    private var matchTitle: String = ""
    private var homeTeam: String = ""
    private var awayTeam: String = ""
    private var venue: String = ""
    private var matchDate: Long = 0L
    
    // UI Components
    private lateinit var matchTitleText: TextView
    private lateinit var matchDateText: TextView
    private lateinit var matchTimeText: TextView
    private lateinit var venueText: TextView
    private lateinit var homeTeamText: TextView
    private lateinit var awayTeamText: TextView
    private lateinit var homeScoreText: TextView
    private lateinit var awayScoreText: TextView
    private lateinit var scoreCard: MaterialCardView
    private lateinit var matchStatusText: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var emptyEventsState: View
    private lateinit var eventsCard: MaterialCardView
    
    // Adapters
    private lateinit var eventsAdapter: MatchEventAdapter
    
    // Match data
    private lateinit var matchDetails: MatchDetails
    private var matchEvents = listOf<MatchEvent>()

    companion object {
        fun newInstance(
            matchId: String,
            matchTitle: String,
            homeTeam: String,
            awayTeam: String,
            venue: String,
            matchDate: Long
        ): MatchDetailsFragment {
            val fragment = MatchDetailsFragment()
            val args = Bundle().apply {
                putString("match_id", matchId)
                putString("match_title", matchTitle)
                putString("home_team", homeTeam)
                putString("away_team", awayTeam)
                putString("venue", venue)
                putLong("match_date", matchDate)
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
            venue = it.getString("venue", "")
            matchDate = it.getLong("match_date", 0L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupEventsRecyclerView()
        loadMatchData()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        // Match info
        matchTitleText = view.findViewById(R.id.matchTitle)
        matchDateText = view.findViewById(R.id.matchDate)
        matchTimeText = view.findViewById(R.id.matchTime)
        venueText = view.findViewById(R.id.venue)
        
        // Teams
        homeTeamText = view.findViewById(R.id.homeTeam)
        awayTeamText = view.findViewById(R.id.awayTeam)
        
        // Score
        homeScoreText = view.findViewById(R.id.homeScore)
        awayScoreText = view.findViewById(R.id.awayScore)
        scoreCard = view.findViewById(R.id.scoreCard)
        
        // Status
        matchStatusText = view.findViewById(R.id.matchStatus)
        
        // Events
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        emptyEventsState = view.findViewById(R.id.emptyEventsState)
        eventsCard = view.findViewById(R.id.eventsCard)
    }
    
    private fun setupEventsRecyclerView() {
        eventsAdapter = MatchEventAdapter(
            onEventClick = { event -> handleEventClick(event) },
            onEventLongClick = { event -> handleEventLongClick(event) }
        )
        
        eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    private fun loadMatchData() {
        // TODO: Backend - Load actual match data
        // Create sample match details for now
        val dateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val date = Date(matchDate)
        
        // Sample data - replace with backend data
        matchDetails = MatchDetails(
            matchId = matchId,
            title = matchTitle,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            venue = venue,
            date = date,
            time = timeFormatter.format(date),
            homeScore = 2, // Sample score - will be 0 for upcoming matches
            awayScore = 2,
            status = MatchStatus.FULL_TIME, // Sample status
            rsvpStats = RSVPStats(12, 3, 2, 1),
            playerAvailability = emptyList(),
            createdBy = "Coach"
        )
        
        // Load sample match events
        loadSampleMatchEvents()
        
        updateUI()
    }

    private fun updateUI() {
        // Match header information
        matchTitleText.text = "${matchDetails.homeTeam} vs ${matchDetails.awayTeam}"
        
        val dateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        matchDateText.text = dateFormatter.format(matchDetails.date)
        
        matchTimeText.text = matchDetails.time
        venueText.text = matchDetails.venue

        // Team names
        homeTeamText.text = matchDetails.homeTeam
        awayTeamText.text = matchDetails.awayTeam

        // Score and status
        updateScoreDisplay()
        updateMatchStatus()
        updateEventsDisplay()
    }

    private fun updateScoreDisplay() {
        when (matchDetails.status) {
            MatchStatus.SCHEDULED -> {
                // Hide score for upcoming matches
                scoreCard.visibility = View.GONE
            }
            MatchStatus.IN_PROGRESS, MatchStatus.PAUSED, MatchStatus.HALF_TIME, MatchStatus.FULL_TIME -> {
                // Show score for live and completed matches
                scoreCard.visibility = View.VISIBLE
                homeScoreText.text = matchDetails.homeScore.toString()
                awayScoreText.text = matchDetails.awayScore.toString()
                
                // Color coding based on result (assuming we're the home team)
                val cardColor = when {
                    matchDetails.status == MatchStatus.FULL_TIME -> {
                        when {
                            matchDetails.homeScore > matchDetails.awayScore -> 
                                ContextCompat.getColor(requireContext(), R.color.success_light)
                            matchDetails.homeScore < matchDetails.awayScore -> 
                                ContextCompat.getColor(requireContext(), R.color.error_light)
                            else -> ContextCompat.getColor(requireContext(), R.color.surface_container)
                        }
                    }
                    else -> ContextCompat.getColor(requireContext(), R.color.primary_light)
                }
                scoreCard.setCardBackgroundColor(cardColor)
            }
            else -> {
                scoreCard.visibility = View.GONE
            }
        }
    }

    private fun updateMatchStatus() {
        val statusText = when (matchDetails.status) {
            MatchStatus.SCHEDULED -> "Upcoming"
            MatchStatus.IN_PROGRESS -> "LIVE"
            MatchStatus.PAUSED -> "Paused"
            MatchStatus.HALF_TIME -> "Half Time"
            MatchStatus.FULL_TIME -> "Full Time"
            MatchStatus.CANCELLED -> "Cancelled"
        }
        
        matchStatusText.text = statusText
        
        // Color coding for status
        val statusColor = when (matchDetails.status) {
            MatchStatus.IN_PROGRESS -> ContextCompat.getColor(requireContext(), R.color.error)
            MatchStatus.SCHEDULED -> ContextCompat.getColor(requireContext(), R.color.primary)
            MatchStatus.FULL_TIME -> ContextCompat.getColor(requireContext(), R.color.success)
            else -> ContextCompat.getColor(requireContext(), R.color.warning)
        }
        
        matchStatusText.setTextColor(statusColor)
    }
    
    private fun loadSampleMatchEvents() {
        // Sample match events based on the image
        matchEvents = listOf(
            // Match start
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.MATCH_START,
                minute = 0,
                createdBy = "Referee"
            ),
            // First goal by Julian Robertson (13)
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.GOAL,
                minute = 13,
                playerId = "13",
                playerName = "Julian Robertson",
                teamId = "home",
                teamName = homeTeam,
                createdBy = "Coach"
            ),
            // Half time
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.HALF_TIME,
                minute = 45,
                createdBy = "Referee"
            ),
            // Opponent red card
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.RED_CARD,
                minute = 52,
                playerId = "opponent1",
                playerName = "Opponent",
                teamId = "away",
                teamName = awayTeam,
                createdBy = "Referee"
            ),
            // Opponent red card (second)
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.RED_CARD,
                minute = 58,
                playerId = "opponent2",
                playerName = "Opponent",
                teamId = "away",
                teamName = awayTeam,
                createdBy = "Referee"
            ),
            // Opponent yellow card
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.YELLOW_CARD,
                minute = 65,
                playerId = "opponent3",
                playerName = "Opponent",
                teamId = "away",
                teamName = awayTeam,
                createdBy = "Referee"
            ),
            // Substitution: Robertson -> Messi
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.SUBSTITUTION,
                minute = 72,
                playerId = "9",
                playerName = "Robertson",
                teamId = "home",
                teamName = homeTeam,
                details = mapOf(
                    "playerOut" to "Robertson",
                    "playerIn" to "Messi"
                ),
                createdBy = "Coach"
            ),
            // Goal by Messi
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.GOAL,
                minute = 78,
                playerId = "12",
                playerName = "Lionardo Messi",
                teamId = "home",
                teamName = homeTeam,
                createdBy = "Coach"
            ),
            // Match end
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.MATCH_END,
                minute = 90,
                createdBy = "Referee"
            )
        )
    }
    
    private fun updateEventsDisplay() {
        if (matchEvents.isEmpty()) {
            eventsCard.visibility = View.GONE
        } else {
            eventsCard.visibility = View.VISIBLE
            eventsAdapter.updateEvents(matchEvents)
            
            // Show/hide empty state
            if (matchEvents.isEmpty()) {
                emptyEventsState.visibility = View.VISIBLE
                eventsRecyclerView.visibility = View.GONE
            } else {
                emptyEventsState.visibility = View.GONE
                eventsRecyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    private fun handleEventClick(event: MatchEvent) {
        // TODO: Show event details
        // Could show a bottom sheet with more details about the event
    }
    
    private fun handleEventLongClick(event: MatchEvent) {
        // TODO: Show event actions (edit/delete for coaches)
        // Could show a context menu with options
    }

    private fun observeViewModel() {
        // TODO: Observe match data changes
        // viewModel.matchDetails.observe(viewLifecycleOwner) { match ->
        //     if (match != null) {
        //         matchDetails = match
        //         updateUI()
        //     }
        // }
    }

    // Method to update match data externally (e.g., from live updates)
    fun updateMatchData(newMatchDetails: MatchDetails) {
        matchDetails = newMatchDetails
        updateUI()
    }
}

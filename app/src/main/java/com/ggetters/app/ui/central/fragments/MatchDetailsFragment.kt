package com.ggetters.app.ui.central.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchStatus
import com.ggetters.app.ui.central.adapters.MatchEventAdapter
import com.ggetters.app.ui.central.viewmodels.MatchDetailsViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Displays a single match: title, date/time, venue, scores, status, and an event timeline.
 *
 * @see <a href="https://developer.android.com/guide/fragments">Fragments</a>
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle">repeatOnLifecycle</a>
 */
@AndroidEntryPoint
class MatchDetailsFragment : Fragment() {

    private val viewModel: MatchDetailsViewModel by viewModels()

    // Args
    private var matchId: String = ""
    private var matchTitle: String = ""
    private var homeTeam: String = ""
    private var awayTeam: String = ""
    private var venue: String = ""
    private var matchDate: Long = 0L

    // UI
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

    // Adapter
    private lateinit var eventsAdapter: MatchEventAdapter

    companion object {
        fun newInstance(eventId: String): MatchDetailsFragment {
            return MatchDetailsFragment().apply {
                arguments = Bundle().apply { putString("event_id", eventId) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString("event_id", "")
            matchTitle = it.getString("match_title", "")
            homeTeam = it.getString("home_team", "")
            awayTeam = it.getString("away_team", "")
            venue = it.getString("venue", "")
            matchDate = it.getLong("match_date", 0L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecycler()
        bindViewModel()
        viewModel.loadMatchDetails(matchId, matchTitle, homeTeam, awayTeam, venue, matchDate)
    }

    private fun initViews(view: View) {
        matchTitleText = view.findViewById(R.id.matchTitle)
        matchDateText = view.findViewById(R.id.matchDate)
        matchTimeText = view.findViewById(R.id.matchTime)
        venueText = view.findViewById(R.id.venue)

        homeTeamText = view.findViewById(R.id.homeTeam)
        awayTeamText = view.findViewById(R.id.awayTeam)

        homeScoreText = view.findViewById(R.id.homeScore)
        awayScoreText = view.findViewById(R.id.awayScore)
        scoreCard = view.findViewById(R.id.scoreCard)

        matchStatusText = view.findViewById(R.id.matchStatus)

        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        emptyEventsState = view.findViewById(R.id.emptyEventsState)
        eventsCard = view.findViewById(R.id.eventsCard)
    }

    private fun setupRecycler() {
        eventsAdapter = MatchEventAdapter(
            onEventClick = { /* TODO: bottom sheet with details */ },
            onEventLongClick = { /* TODO: coach actions (edit/delete) */ }
        )
        eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    private fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.matchDetails.collect { details ->
                        details?.let { renderMatch(it) }
                    }
                }
                launch {
                    viewModel.events.collect { list ->
                        renderEvents(list)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            // TODO: show Snackbar/Toast
                        }
                    }
                }
            }
        }
    }

    private fun renderMatch(details: MatchDetails) {
        matchTitleText.text = "${details.homeTeam} vs ${details.awayTeam}"

        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd")
            .withZone(ZoneId.systemDefault())
        matchDateText.text = dateFormatter.format(details.date)

        matchTimeText.text = details.time
        venueText.text = details.venue
        homeTeamText.text = details.homeTeam
        awayTeamText.text = details.awayTeam

        renderScore(details)
        renderStatus(details)
    }

    private fun renderScore(details: MatchDetails) {
        when (details.status) {
            MatchStatus.SCHEDULED -> scoreCard.visibility = View.GONE
            MatchStatus.IN_PROGRESS, MatchStatus.PAUSED, MatchStatus.HALF_TIME, MatchStatus.FULL_TIME -> {
                scoreCard.visibility = View.VISIBLE
                homeScoreText.text = details.homeScore.toString()
                awayScoreText.text = details.awayScore.toString()

                val bgColor = when {
                    details.status == MatchStatus.FULL_TIME -> when {
                        details.homeScore > details.awayScore ->
                            ContextCompat.getColor(requireContext(), R.color.success_light)
                        details.homeScore < details.awayScore ->
                            ContextCompat.getColor(requireContext(), R.color.error_light)
                        else -> ContextCompat.getColor(requireContext(), R.color.surface_container)
                    }
                    else -> ContextCompat.getColor(requireContext(), R.color.primary_light)
                }
                scoreCard.setCardBackgroundColor(bgColor)
            }
            else -> scoreCard.visibility = View.GONE
        }
    }

    private fun renderStatus(details: MatchDetails) {
        val text = when (details.status) {
            MatchStatus.SCHEDULED -> "Upcoming"
            MatchStatus.IN_PROGRESS -> "LIVE"
            MatchStatus.PAUSED -> "Paused"
            MatchStatus.HALF_TIME -> "Half Time"
            MatchStatus.FULL_TIME -> "Full Time"
            MatchStatus.CANCELLED -> "Cancelled"
        }
        matchStatusText.text = text

        val color = when (details.status) {
            MatchStatus.IN_PROGRESS -> ContextCompat.getColor(requireContext(), R.color.error)
            MatchStatus.SCHEDULED   -> ContextCompat.getColor(requireContext(), R.color.primary)
            MatchStatus.FULL_TIME   -> ContextCompat.getColor(requireContext(), R.color.success)
            else                    -> ContextCompat.getColor(requireContext(), R.color.warning)
        }
        matchStatusText.setTextColor(color)
    }

    private fun renderEvents(events: List<MatchEvent>) {
        if (events.isEmpty()) {
            eventsCard.visibility = View.GONE
            emptyEventsState.visibility = View.VISIBLE
            eventsRecyclerView.visibility = View.GONE
            return
        }
        eventsCard.visibility = View.VISIBLE
        emptyEventsState.visibility = View.GONE
        eventsRecyclerView.visibility = View.VISIBLE
        eventsAdapter.updateEvents(events)
    }

    /** Optional external hook if you keep it elsewhere */
    fun updateMatchData(newMatchDetails: MatchDetails) {
        renderMatch(newMatchDetails)
    }
}

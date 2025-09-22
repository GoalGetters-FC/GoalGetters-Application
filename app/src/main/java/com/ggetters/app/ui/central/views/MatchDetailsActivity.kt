package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ggetters.app.R
import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchStatus
import com.ggetters.app.data.model.RSVPStats
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.model.RSVPStatus

import com.ggetters.app.ui.central.viewmodels.MatchDetailsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// TODO: Backend - Implement real-time match data synchronization
// TODO: Backend - Add match data caching for offline access
// TODO: Backend - Implement player availability notifications
// TODO: Backend - Add match sharing and social media integration
// TODO: Backend - Implement match analytics and insights
// TODO: Backend - Add match template management
// TODO: Backend - Implement team management and permissions

@AndroidEntryPoint
class MatchDetailsActivity : AppCompatActivity() {

    private val viewModel: MatchDetailsViewModel by viewModels()
    private lateinit var matchDetails: MatchDetails

    // UI Components
    private lateinit var matchTitleText: android.widget.TextView
    private lateinit var venueText: android.widget.TextView
    private lateinit var dateTimeText: android.widget.TextView
    private lateinit var homeTeamText: android.widget.TextView
    private lateinit var awayTeamText: android.widget.TextView
    private lateinit var scoreDisplay: android.view.View
    private lateinit var rsvpStatsCard: android.view.View
    private lateinit var availableCountText: android.widget.TextView
    private lateinit var maybeCountText: android.widget.TextView
    private lateinit var unavailableCountText: android.widget.TextView
    private var rsvpSummaryText: android.widget.TextView? = null
    private lateinit var buildLineupButton: MaterialButton
    private lateinit var viewRosterButton: MaterialButton
    private lateinit var editMatchButton: MaterialButton
    
    // RSVP Chips
    private lateinit var rsvpGoingChip: Chip
    private lateinit var rsvpMaybeChip: Chip
    private lateinit var rsvpNotGoingChip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)
        
        setupToolbar()
        initializeViews()
        loadMatchData()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Match Details"
        }
    }

    private fun initializeViews() {
        // Header back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
        
        matchTitleText = findViewById(R.id.matchTitle)
        venueText = findViewById(R.id.venue)
        dateTimeText = findViewById(R.id.dateTime)
        homeTeamText = findViewById(R.id.homeTeamName)
        awayTeamText = findViewById(R.id.awayTeamName)
        scoreDisplay = findViewById(R.id.scoreSection)
        rsvpStatsCard = findViewById(R.id.rsvpChipGroup) // Using chip group as RSVP container
        availableCountText = findViewById(R.id.availableCount)
        maybeCountText = findViewById(R.id.maybeCount)
        unavailableCountText = findViewById(R.id.unavailableCount)
        rsvpSummaryText = null // Not in new layout
        buildLineupButton = findViewById(R.id.buildLineupButton)
        viewRosterButton = findViewById(R.id.viewRosterButton)
        editMatchButton = findViewById(R.id.editMatchButton)
        
        // RSVP chip elements
        rsvpGoingChip = findViewById(R.id.rsvpGoingChip)
        rsvpMaybeChip = findViewById(R.id.rsvpMaybeChip)
        rsvpNotGoingChip = findViewById(R.id.rsvpNotGoingChip)
    }

    private fun loadMatchData() {
        // TODO: Backend - Load match data from backend using match ID
        // TODO: Backend - Implement proper error handling for match loading
        // TODO: Backend - Add loading states and progress indicators
        
        // Get match data from intent (passed from calendar)
        val eventId = intent.getStringExtra("event_id") ?: ""
        val eventTitle = intent.getStringExtra("event_title") ?: "Match"
        val eventVenue = intent.getStringExtra("event_venue") ?: "Stadium"
        val eventOpponent = intent.getStringExtra("event_opponent") ?: "Opponent"
        val eventDate = intent.getLongExtra("event_date", System.currentTimeMillis())
        val eventTime = intent.getStringExtra("event_time") ?: "15:00"

        // Create sample match details (TODO: Replace with backend data)
        matchDetails = createSampleMatchDetails(eventId, eventTitle, eventVenue, eventOpponent, eventDate, eventTime)
        
        updateUI()
    }

    private fun createSampleMatchDetails(
        eventId: String,
        title: String,
        venue: String,
        opponent: String,
        dateMillis: Long,
        time: String
    ): MatchDetails {
        val samplePlayers = listOf(
            RosterPlayer(
                playerId = "1",
                playerName = "Alice",
                jerseyNumber = 11,
                position = "GK",
                status = RSVPStatus.AVAILABLE
            )
        )

        val available = samplePlayers.count { it.status == RSVPStatus.AVAILABLE }
        val maybe = samplePlayers.count { it.status == RSVPStatus.MAYBE }
        val unavailable = samplePlayers.count { it.status == RSVPStatus.UNAVAILABLE }
        val notResponded = samplePlayers.count { it.status == RSVPStatus.NOT_RESPONDED }

        val instantDate = Instant.ofEpochMilli(dateMillis)

        return MatchDetails(
            matchId = eventId,
            title = title,
            homeTeam = "Goal Getters FC",
            awayTeam = opponent,
            venue = venue,
            date = instantDate,
            time = time,
            homeScore = 0,
            awayScore = 0,
            status = MatchStatus.SCHEDULED,
            rsvpStats = RSVPStats(
                available = available,
                maybe = maybe,
                unavailable = unavailable,
                notResponded = notResponded
            ),
            playerAvailability = samplePlayers,
            createdBy = "Coach",
            createdAt = Instant.now()
        )
    }

    private fun updateUI() {
        matchTitleText.text = matchDetails.title
        venueText.text = matchDetails.venue

        // ✅ Use DateTimeFormatter for Instant
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault())
        dateTimeText.text = "${dateFormatter.format(matchDetails.date)} at ${matchDetails.time}"

        homeTeamText.text = matchDetails.homeTeam
        awayTeamText.text = matchDetails.awayTeam

        if (matchDetails.isMatchStarted()) {
            scoreDisplay.visibility = View.VISIBLE
            findViewById<TextView>(R.id.homeScore).text = matchDetails.homeScore.toString()
            findViewById<TextView>(R.id.awayScore).text = matchDetails.awayScore.toString()
        } else {
            scoreDisplay.visibility = View.GONE
        }

        findViewById<TextView>(R.id.matchStatus).text = when (matchDetails.status) {
            MatchStatus.SCHEDULED -> "Upcoming"
            MatchStatus.IN_PROGRESS -> "Live"
            MatchStatus.PAUSED -> "Paused"
            MatchStatus.HALF_TIME -> "Half Time"
            MatchStatus.FULL_TIME -> "Full Time"
            MatchStatus.CANCELLED -> "Cancelled"
        }

        updateRSVPStats()
        updateButtonStates()
    }

    private fun updateRSVPStats() {
        val stats = matchDetails.rsvpStats
        
        availableCountText.text = stats.available.toString()
        maybeCountText.text = stats.maybe.toString()
        unavailableCountText.text = stats.unavailable.toString()
        
        // Show RSVP card (always visible in new layout)
        rsvpStatsCard.visibility = View.VISIBLE
    }

    private fun updateButtonStates() {
        when (matchDetails.status) {
            MatchStatus.SCHEDULED -> {
                buildLineupButton.isEnabled = matchDetails.canStartMatch()
                buildLineupButton.text = if (matchDetails.canStartMatch()) {
                    "Build Lineup"
                } else {
                    "Need ${11 - matchDetails.rsvpStats.available} more players"
                }
                editMatchButton.visibility = View.VISIBLE
            }
            MatchStatus.IN_PROGRESS, MatchStatus.PAUSED, MatchStatus.HALF_TIME -> {
                buildLineupButton.text = "View Match Control"
                buildLineupButton.isEnabled = true
                editMatchButton.visibility = View.GONE
            }
            MatchStatus.FULL_TIME -> {
                buildLineupButton.text = "View Match Results"
                buildLineupButton.isEnabled = true
                editMatchButton.visibility = View.GONE
            }
            MatchStatus.CANCELLED -> {
                buildLineupButton.text = "Match Cancelled"
                buildLineupButton.isEnabled = false
                editMatchButton.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        buildLineupButton.setOnClickListener {
            if (::matchDetails.isInitialized) {
                when (matchDetails.status) {
                    MatchStatus.SCHEDULED -> {
                        if (matchDetails.canStartMatch()) {
                            navigateToRoster()
                        } else {
                            showInsufficientPlayersMessage()
                        }
                    }
                    MatchStatus.IN_PROGRESS, MatchStatus.PAUSED, MatchStatus.HALF_TIME -> {
                        navigateToMatchControl()
                    }
                    MatchStatus.FULL_TIME -> {
                        navigateToPostMatch()
                    }
                    else -> {
                        // Handle other states
                    }
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Match data not loaded yet", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewRosterButton.setOnClickListener {
            navigateToRoster()
        }

        editMatchButton.setOnClickListener {
            editMatch()
        }

        // RSVP chip listeners
        rsvpGoingChip.setOnClickListener {
            handleRSVPResponse(RSVPStatus.AVAILABLE)
        }
        
        rsvpMaybeChip.setOnClickListener {
            handleRSVPResponse(RSVPStatus.MAYBE)
        }
        
        rsvpNotGoingChip.setOnClickListener {
            handleRSVPResponse(RSVPStatus.UNAVAILABLE)
        }

        scoreDisplay.setOnClickListener {
            if (::matchDetails.isInitialized && matchDetails.isMatchStarted()) {
                navigateToMatchControl()
            }
        }
    }

    private fun handleRSVPResponse(status: RSVPStatus) {
        // TODO: Backend - Update RSVP status for current user
        val statusText = when (status) {
            RSVPStatus.AVAILABLE -> "available"
            RSVPStatus.UNAVAILABLE -> "unavailable"
            RSVPStatus.MAYBE -> "maybe"
            RSVPStatus.NOT_RESPONDED -> "no response"
        }
        
        Snackbar.make(findViewById(android.R.id.content), 
            "RSVP status set to: $statusText", Snackbar.LENGTH_SHORT).show()
        
        // Update chip selection states
        highlightRSVPChip(rsvpGoingChip, status == RSVPStatus.AVAILABLE)
        highlightRSVPChip(rsvpMaybeChip, status == RSVPStatus.MAYBE)
        highlightRSVPChip(rsvpNotGoingChip, status == RSVPStatus.UNAVAILABLE)
        
        // Update match data and UI
        // TODO: Backend - Update actual match data with new RSVP
    }

    private fun highlightRSVPChip(chip: Chip, isSelected: Boolean) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.primary)
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_on_accent))
        } else {
            chip.setChipBackgroundColorResource(R.color.surface_variant)
            chip.setTextColor(ContextCompat.getColor(this, R.color.on_surface))
        }
    }

    private fun navigateToRoster() {
        // TODO: Backend - Pass match data to roster activity
        if (::matchDetails.isInitialized) {
            val intent = Intent(this, MatchRosterActivity::class.java).apply {
                putExtra("match_id", matchDetails.matchId)
                putExtra("match_title", matchDetails.title)
                putExtra("home_team", matchDetails.homeTeam)
                putExtra("away_team", matchDetails.awayTeam)
            }
            startActivity(intent)
        } else {
            Snackbar.make(findViewById(android.R.id.content), 
                "Match data not loaded yet", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMatchControl() {
        val intent = Intent(this, MatchControlActivity::class.java).apply {
            putExtra("event_id", matchDetails.matchId)
            putExtra("event_title", matchDetails.title)
            putExtra("event_opponent", matchDetails.awayTeam)
            putExtra("event_venue", matchDetails.venue)
            putExtra("event_date", matchDetails.date.toEpochMilli())
            putExtra("event_time", matchDetails.time)
        }
        startActivity(intent)
    }


    private fun navigateToPostMatch() {
        // TODO: Backend - Navigate to post-match results
        Snackbar.make(findViewById(android.R.id.content), 
            "Post-match results coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun editMatch() {
        // TODO: Backend - Navigate to match editing
        Snackbar.make(findViewById(android.R.id.content), 
            "Match editing coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showInsufficientPlayersMessage() {
        val needed = 11 - matchDetails.rsvpStats.available
        Snackbar.make(findViewById(android.R.id.content), 
            "Need $needed more available players to start match", Snackbar.LENGTH_LONG).show()
    }

    private fun observeViewModel() {
        // TODO: Backend - Observe match data changes from ViewModel
        // TODO: Backend - Handle loading states
        // TODO: Backend - Handle error states
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onResume() {
        super.onResume()
        // TODO: Backend - Refresh match data when returning from other screens
        // For now, just update the UI
        if (::matchDetails.isInitialized) {
            updateUI()
        }
    }
}

package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.viewmodels.MatchDetailsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
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
    private lateinit var scoreDisplay: android.widget.TextView
    private lateinit var rsvpStatsCard: MaterialCardView
    private lateinit var availableCountText: android.widget.TextView
    private lateinit var maybeCountText: android.widget.TextView
    private lateinit var unavailableCountText: android.widget.TextView
    private lateinit var notRespondedCountText: android.widget.TextView
    private lateinit var rsvpSummaryText: android.widget.TextView
    private lateinit var buildLineupButton: MaterialButton
    private lateinit var viewRosterButton: MaterialButton
    private lateinit var editMatchButton: MaterialButton

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
        matchTitleText = findViewById(R.id.matchTitle)
        venueText = findViewById(R.id.venue)
        dateTimeText = findViewById(R.id.dateTime)
        homeTeamText = findViewById(R.id.homeTeam)
        awayTeamText = findViewById(R.id.awayTeam)
        scoreDisplay = findViewById(R.id.scoreDisplay)
        rsvpStatsCard = findViewById(R.id.rsvpStatsCard)
        availableCountText = findViewById(R.id.availableCount)
        maybeCountText = findViewById(R.id.maybeCount)
        unavailableCountText = findViewById(R.id.unavailableCount)
        notRespondedCountText = findViewById(R.id.notRespondedCount)
        rsvpSummaryText = findViewById(R.id.rsvpSummary)
        buildLineupButton = findViewById(R.id.buildLineupButton)
        viewRosterButton = findViewById(R.id.viewRosterButton)
        editMatchButton = findViewById(R.id.editMatchButton)
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
        // TODO: Backend - Replace with real player data from backend
        val samplePlayers = listOf(
            PlayerAvailability("1", "John Smith", "GK", 1, RSVPStatus.AVAILABLE),
            PlayerAvailability("2", "Mike Johnson", "CB", 4, RSVPStatus.AVAILABLE),
            PlayerAvailability("3", "David Wilson", "CB", 5, RSVPStatus.AVAILABLE),
            PlayerAvailability("4", "Chris Brown", "LB", 3, RSVPStatus.MAYBE),
            PlayerAvailability("5", "Tom Davis", "RB", 2, RSVPStatus.AVAILABLE),
            PlayerAvailability("6", "Alex Miller", "CM", 8, RSVPStatus.AVAILABLE),
            PlayerAvailability("7", "Sam Wilson", "CM", 6, RSVPStatus.UNAVAILABLE),
            PlayerAvailability("8", "Jake Taylor", "CM", 10, RSVPStatus.AVAILABLE),
            PlayerAvailability("9", "Ben Moore", "LW", 11, RSVPStatus.AVAILABLE),
            PlayerAvailability("10", "Luke Jackson", "ST", 9, RSVPStatus.AVAILABLE),
            PlayerAvailability("11", "Ryan White", "RW", 7, RSVPStatus.MAYBE),
            PlayerAvailability("12", "Mark Lewis", "SUB", 12, RSVPStatus.AVAILABLE),
            PlayerAvailability("13", "Paul Clark", "SUB", 13, RSVPStatus.NOT_RESPONDED),
            PlayerAvailability("14", "Steve Hall", "SUB", 14, RSVPStatus.NOT_RESPONDED),
            PlayerAvailability("15", "Nick Allen", "SUB", 15, RSVPStatus.UNAVAILABLE)
        )

        val available = samplePlayers.count { it.status == RSVPStatus.AVAILABLE }
        val maybe = samplePlayers.count { it.status == RSVPStatus.MAYBE }
        val unavailable = samplePlayers.count { it.status == RSVPStatus.UNAVAILABLE }
        val notResponded = samplePlayers.count { it.status == RSVPStatus.NOT_RESPONDED }

        return MatchDetails(
            matchId = eventId,
            title = title,
            homeTeam = "Goal Getters FC",
            awayTeam = opponent,
            venue = venue,
            date = Date(dateMillis),
            time = time,
            homeScore = 0,
            awayScore = 0,
            status = MatchStatus.SCHEDULED,
            rsvpStats = RSVPStats(available, maybe, unavailable, notResponded),
            playerAvailability = samplePlayers,
            createdBy = "Coach"
        )
    }

    private fun updateUI() {
        // Match header information
        matchTitleText.text = matchDetails.title
        venueText.text = matchDetails.venue
        dateTimeText.text = matchDetails.getFormattedDateTime()

        // Team names
        homeTeamText.text = matchDetails.homeTeam
        awayTeamText.text = matchDetails.awayTeam

        // Score display
        scoreDisplay.text = matchDetails.getFormattedScore()
        
        // RSVP Statistics
        updateRSVPStats()

        // Button states
        updateButtonStates()
    }

    private fun updateRSVPStats() {
        val stats = matchDetails.rsvpStats
        
        availableCountText.text = stats.available.toString()
        maybeCountText.text = stats.maybe.toString()
        unavailableCountText.text = stats.unavailable.toString()
        notRespondedCountText.text = stats.notResponded.toString()
        
        rsvpSummaryText.text = stats.getFormattedSummary()

        // Update card background color based on availability
        val availabilityPercentage = stats.getAvailabilityPercentage()
        val cardColor = when {
            availabilityPercentage >= 75 -> ContextCompat.getColor(this, R.color.success_light)
            availabilityPercentage >= 50 -> ContextCompat.getColor(this, R.color.warning_light)
            else -> ContextCompat.getColor(this, R.color.error_light)
        }
        rsvpStatsCard.setCardBackgroundColor(cardColor)
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
        }

        viewRosterButton.setOnClickListener {
            navigateToRoster()
        }

        editMatchButton.setOnClickListener {
            editMatch()
        }

        scoreDisplay.setOnClickListener {
            if (matchDetails.isMatchStarted()) {
                navigateToMatchControl()
            }
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
        // TODO: Backend - Pass match state to control activity
        val intent = Intent(this, MatchControlActivity::class.java).apply {
            putExtra("event_id", matchDetails.matchId)
            putExtra("event_title", matchDetails.title)
            putExtra("event_opponent", matchDetails.awayTeam)
            putExtra("event_venue", matchDetails.venue)
            putExtra("event_date", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(matchDetails.date))
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
        onBackPressed()
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

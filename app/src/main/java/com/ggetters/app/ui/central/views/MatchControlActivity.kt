package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.sheets.RecordEventBottomSheet
import com.ggetters.app.ui.central.sheets.TimerControlBottomSheet
import com.ggetters.app.ui.central.viewmodels.MatchControlViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MatchControlActivity : AppCompatActivity() {

    private val model: MatchControlViewModel by viewModels()
    private lateinit var matchState: MatchState
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    // UI Elements
    private lateinit var homeTeamName: TextView
    private lateinit var awayTeamName: TextView
    private lateinit var homeScore: TextView
    private lateinit var awayScore: TextView
    private lateinit var matchTimer: TextView
    private lateinit var matchStatus: TextView
    private lateinit var btnStartMatch: MaterialButton
    private lateinit var btnPauseMatch: MaterialButton
    private lateinit var btnEndMatch: MaterialButton
    private lateinit var btnUndo: MaterialButton
    private lateinit var btnTimerControl: MaterialButton
    private lateinit var btnAnalytics: MaterialButton
    private lateinit var btnCancelMatch: MaterialButton
    private lateinit var btnRecordEvent: MaterialButton
    private lateinit var btnSettings: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_control)

        setupViews()
        setupClickListeners()
        loadMatchData()
        startTimer()
    }

    private fun setupViews() {
        // Header
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Score and timer elements
        homeTeamName = findViewById(R.id.homeTeamName)
        awayTeamName = findViewById(R.id.awayTeamName)
        homeScore = findViewById(R.id.homeScore)
        awayScore = findViewById(R.id.awayScore)
        matchTimer = findViewById(R.id.matchTimer)
        matchStatus = findViewById(R.id.matchStatus)

        // Control buttons
        btnStartMatch = findViewById(R.id.btnStartMatch)
        btnPauseMatch = findViewById(R.id.btnPauseMatch)
        btnEndMatch = findViewById(R.id.btnEndMatch)
        btnUndo = findViewById(R.id.btnUndo)
        btnTimerControl = findViewById(R.id.btnTimerControl)
        btnAnalytics = findViewById(R.id.btnAnalytics)
        btnCancelMatch = findViewById(R.id.btnCancelMatch)
        btnRecordEvent = findViewById(R.id.btnRecordEvent)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupClickListeners() {
        // Match control buttons
        btnStartMatch.setOnClickListener {
            handleStartMatch()
        }

        btnPauseMatch.setOnClickListener {
            handlePauseMatch()
        }

        btnEndMatch.setOnClickListener {
            handleEndMatch()
        }

        btnUndo.setOnClickListener {
            handleUndoLastEvent()
        }

        btnTimerControl.setOnClickListener {
            showTimerControlDialog()
        }

        btnAnalytics.setOnClickListener {
            showAnalytics()
        }

        btnCancelMatch.setOnClickListener {
            showCancelMatchConfirmation()
        }

        btnRecordEvent.setOnClickListener {
            showRecordEventDialog()
        }

        btnSettings.setOnClickListener {
            showSettings()
        }
    }

    private fun loadMatchData() {
        // Get event data from intent (passed from EventDetailsBottomSheet)
        val eventId = intent.getStringExtra("event_id")
        val eventTitle = intent.getStringExtra("event_title")
        val eventOpponent = intent.getStringExtra("event_opponent")
        val eventVenue = intent.getStringExtra("event_venue")
        val eventDate = intent.getStringExtra("event_date")
        val eventTime = intent.getStringExtra("event_time")
        
        // TODO: Backend - Load match data from backend using eventId
        val matchId = eventId ?: "sample_match"
        
        // Use actual event data if available, otherwise use sample data
        matchState = MatchState(
            matchId = matchId,
            status = MatchStatus.SCHEDULED,
            homeTeam = "Goal Getters FC",
            awayTeam = eventOpponent ?: "Tigers FC",
            homeScore = 0,
            awayScore = 0,
            lineup = createSampleLineup()
        )

        // Update header with actual event details
        if (eventTitle != null) {
            findViewById<TextView>(R.id.matchTitle).text = eventTitle
        }
        
        if (eventVenue != null) {
            findViewById<TextView>(R.id.matchVenue).text = eventVenue
        }

        updateUI()
    }

    private fun createSampleLineup(): MatchLineup {
        // TODO: Backend - Load actual lineup from backend
        val startingPlayers = listOf(
            LineupPlayer("1", "John Smith", "GK", 1),
            LineupPlayer("2", "Mike Johnson", "LB", 2),
            LineupPlayer("3", "David Wilson", "CB", 3),
            LineupPlayer("4", "Chris Brown", "CB", 4),
            LineupPlayer("5", "Tom Davis", "RB", 5),
            LineupPlayer("6", "James Miller", "CM", 6),
            LineupPlayer("7", "Robert Garcia", "CM", 7),
            LineupPlayer("8", "Daniel Martinez", "CM", 8),
            LineupPlayer("9", "Kevin Rodriguez", "LW", 9),
            LineupPlayer("10", "Steven Lopez", "ST", 10),
            LineupPlayer("11", "Andrew Gonzalez", "RW", 11)
        )

        val substitutes = listOf(
            LineupPlayer("12", "Ryan Hernandez", "GK", 12),
            LineupPlayer("13", "Brandon Torres", "CB", 13),
            LineupPlayer("14", "Nathan Flores", "CM", 14),
            LineupPlayer("15", "Timothy Collins", "ST", 15)
        )

        return MatchLineup(
            matchId = matchState.matchId,
            formation = "4-3-3",
            startingPlayers = startingPlayers,
            substitutes = substitutes
        )
    }

    private fun updateUI() {
        // Update header
        findViewById<TextView>(R.id.matchTitle).text = "Track Event - ${matchState.homeTeam}"

        // Update team names and scores
        homeTeamName.text = matchState.homeTeam
        awayTeamName.text = matchState.awayTeam
        homeScore.text = matchState.homeScore.toString()
        awayScore.text = matchState.awayScore.toString()

        // Update timer
        matchTimer.text = matchState.getMatchDuration()

        // Update status
        matchStatus.text = matchState.status.name.replace("_", " ").capitalize()

        // Update button visibility based on match status
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        when (matchState.status) {
            MatchStatus.SCHEDULED -> {
                btnStartMatch.visibility = View.VISIBLE
                btnPauseMatch.visibility = View.GONE
                btnEndMatch.visibility = View.GONE
                btnRecordEvent.visibility = View.GONE
            }
            MatchStatus.IN_PROGRESS -> {
                btnStartMatch.visibility = View.GONE
                btnPauseMatch.visibility = View.VISIBLE
                btnEndMatch.visibility = View.VISIBLE
                btnRecordEvent.visibility = View.VISIBLE
            }
            MatchStatus.PAUSED -> {
                btnStartMatch.visibility = View.GONE
                btnPauseMatch.visibility = View.VISIBLE
                btnEndMatch.visibility = View.VISIBLE
                btnRecordEvent.visibility = View.VISIBLE
            }
            MatchStatus.FULL_TIME -> {
                btnStartMatch.visibility = View.GONE
                btnPauseMatch.visibility = View.GONE
                btnEndMatch.visibility = View.GONE
                btnRecordEvent.visibility = View.GONE
            }
            else -> {
                // Handle other statuses
            }
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (matchState.isMatchActive() && !matchState.isPaused) {
                    updateUI()
                }
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun handleStartMatch() {
        // TODO: Backend - Start match in backend
        matchState = matchState.copy(
            status = MatchStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(),
            isPaused = false
        )
        
        // Add match start event
        val startEvent = MatchEvent(
            matchId = matchState.matchId,
            eventType = MatchEventType.MATCH_START,
            minute = 0,
            createdBy = "Coach"
        )
        
        matchState = matchState.copy(
            events = matchState.events + startEvent
        )
        
        updateUI()
        Snackbar.make(findViewById(android.R.id.content), "Match started!", Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePauseMatch() {
        // TODO: Backend - Pause/resume match in backend
        if (matchState.status == MatchStatus.IN_PROGRESS) {
            matchState = matchState.copy(
                status = MatchStatus.PAUSED,
                isPaused = true
            )
            btnPauseMatch.text = "Resume"
            btnPauseMatch.setIconResource(R.drawable.ic_unicons_play_24)
        } else {
            matchState = matchState.copy(
                status = MatchStatus.IN_PROGRESS,
                isPaused = false
            )
            btnPauseMatch.text = "Pause"
            btnPauseMatch.setIconResource(R.drawable.ic_unicons_pause_24)
        }
        
        updateUI()
        val action = if (matchState.isPaused) "paused" else "resumed"
        Snackbar.make(findViewById(android.R.id.content), "Match $action", Snackbar.LENGTH_SHORT).show()
    }

    private fun handleEndMatch() {
        // TODO: Backend - End match in backend
        matchState = matchState.copy(
            status = MatchStatus.FULL_TIME,
            endTime = System.currentTimeMillis(),
            isPaused = false
        )
        
        // Add match end event
        val endEvent = MatchEvent(
            matchId = matchState.matchId,
            eventType = MatchEventType.MATCH_END,
            minute = matchState.currentMinute,
            createdBy = "Coach"
        )
        
        matchState = matchState.copy(
            events = matchState.events + endEvent
        )
        
        updateUI()
        showPostMatchSummary()
    }

    private fun handleUndoLastEvent() {
        // TODO: Backend - Undo last event in backend
        if (matchState.events.isNotEmpty()) {
            val lastEvent = matchState.events.last()
            matchState = matchState.copy(
                events = matchState.events.dropLast(1)
            )
            
            // Handle score reversal if it was a goal
            if (lastEvent.eventType == MatchEventType.GOAL) {
                // TODO: Backend - Reverse goal in score
                Snackbar.make(findViewById(android.R.id.content), "Goal reversed", Snackbar.LENGTH_SHORT).show()
            }
            
            updateUI()
            Snackbar.make(findViewById(android.R.id.content), "Last event undone", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "No events to undo", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showTimerControlDialog() {
        // TODO: Backend - Show timer control bottom sheet
        val timerSheet = TimerControlBottomSheet.newInstance(matchState.currentMinute)
        timerSheet.show(supportFragmentManager, "timer_control")
    }

    private fun showAnalytics() {
        // TODO: Backend - Show match analytics/statistics
        Snackbar.make(findViewById(android.R.id.content), "Analytics coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showCancelMatchConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Match")
            .setMessage("Are you sure you want to cancel this match? This action cannot be undone.")
            .setPositiveButton("Cancel Match") { _, _ ->
                handleCancelMatch()
            }
            .setNegativeButton("Keep Match", null)
            .show()
    }

    private fun handleCancelMatch() {
        // TODO: Backend - Cancel match in backend
        matchState = matchState.copy(
            status = MatchStatus.CANCELLED,
            endTime = System.currentTimeMillis()
        )
        
        updateUI()
        Snackbar.make(findViewById(android.R.id.content), "Match cancelled", Snackbar.LENGTH_SHORT).show()
        
        // Navigate back to calendar
        finish()
    }

    private fun showRecordEventDialog() {
        // TODO: Backend - Show record event bottom sheet
        val eventSheet = RecordEventBottomSheet.newInstance(matchState.matchId, matchState.currentMinute)
        eventSheet.show(supportFragmentManager, "record_event")
    }

    private fun showSettings() {
        // TODO: Backend - Show match settings
        Snackbar.make(findViewById(android.R.id.content), "Settings coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showPostMatchSummary() {
        // TODO: Backend - Navigate to post-match summary screen
        Snackbar.make(findViewById(android.R.id.content), "Match ended! Viewing post-match summary...", Snackbar.LENGTH_LONG).show()
        
        // Navigate to post-match summary
        // Intent(this, PostMatchSummaryActivity::class.java).apply {
        //     putExtra("match_id", matchState.matchId)
        //     startActivity(this)
        // }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
} 
package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.extensions.navigateBack
import com.ggetters.app.ui.central.adapters.MatchEventAdapter
import com.ggetters.app.ui.central.models.*
import com.ggetters.app.ui.central.sheets.MatchdayBottomSheet
import com.ggetters.app.ui.central.sheets.RecordEventBottomSheet
import com.ggetters.app.ui.central.sheets.TimerControlBottomSheet
import com.ggetters.app.ui.central.viewmodels.MatchControlViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

// TODO: Backend - Implement real-time match data synchronization
// TODO: Backend - Add match analytics and performance tracking
// TODO: Backend - Implement match recording and video integration
// TODO: Backend - Add match statistics and reporting
// TODO: Backend - Implement match sharing and social features
// TODO: Backend - Add match export and data backup
// TODO: Backend - Implement match templates and presets
// TODO: Backend - Add match collaboration and multi-coach support
// TODO: Backend - Implement match notifications and alerts

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
    private lateinit var matchStatusChip: Chip
    private lateinit var btnStartMatch: MaterialButton
    private lateinit var btnPauseMatch: MaterialButton
    private lateinit var btnEndMatch: MaterialButton
    private lateinit var btnUndo: MaterialButton
    private lateinit var btnTimerControl: MaterialButton
    private lateinit var btnAnalytics: MaterialButton
    private lateinit var btnCancelMatch: MaterialButton
    private lateinit var btnRecordEvent: MaterialButton
    private lateinit var btnSettings: MaterialButton
    // TODO: Add when UI is redesigned
    // private lateinit var eventsRecyclerView: RecyclerView
    // private lateinit var quickActionsFab: FloatingActionButton
    
    // Adapters
    // TODO: Add when events UI is implemented
    // private lateinit var eventsAdapter: MatchEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_control)

        // Enable smooth activity transitions
        supportPostponeEnterTransition()
        supportStartPostponedEnterTransition()

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadMatchData()
        startTimer()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Match Control"
        }
    }

    private fun setupViews() {
        // Header
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { navigateBack() }

        // Score and timer elements
        homeTeamName = findViewById(R.id.homeTeamName)
        awayTeamName = findViewById(R.id.awayTeamName)
        homeScore = findViewById(R.id.homeScore)
        awayScore = findViewById(R.id.awayScore)
        matchTimer = findViewById(R.id.matchTimer)
        matchStatus = findViewById(R.id.matchStatus)
        // TODO: Add these UI elements to the layout when redesigning
        // matchStatusChip = findViewById(R.id.matchStatusChip)
        // eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        // quickActionsFab = findViewById(R.id.quickActionsFab)

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

    private fun setupRecyclerView() {
        // TODO: Setup RecyclerView when events UI is added to layout
        // eventsAdapter = MatchEventAdapter(
        //     onEventClick = { event -> showEventDetails(event) },
        //     onEventLongClick = { event -> showEventActions(event) }
        // )
        // 
        // eventsRecyclerView.apply {
        //     layoutManager = LinearLayoutManager(this@MatchControlActivity).apply {
        //         reverseLayout = true // Show newest events first
        //         stackFromEnd = true
        //     }
        //     adapter = eventsAdapter
        // }
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
        
        // TODO: Enhanced floating action button for quick actions
        // quickActionsFab.setOnClickListener {
        //     showQuickActionsBottomSheet()
        // }
        
        // Score click listeners for quick score updates
        homeScore.setOnClickListener {
            showScoreUpdateDialog(true) // true for home team
        }
        
        awayScore.setOnClickListener {
            showScoreUpdateDialog(false) // false for away team
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
        // TODO: Backend - Implement match data caching for offline access
        // TODO: Backend - Add match data synchronization across devices
        // TODO: Backend - Implement match state persistence and recovery
        // TODO: Backend - Add match data validation and integrity checks
        val matchId = eventId ?: "sample_match"
        
        // Initialize matchState first before using it in createSampleLineup
        matchState = MatchState(
            matchId = matchId,
            status = MatchStatus.SCHEDULED,
            homeTeam = "Goal Getters FC",
            awayTeam = eventOpponent ?: "Tigers FC",
            homeScore = 0,
            awayScore = 0,
            lineup = createSampleLineup(matchId)
        )

        // Update header with actual event details
        if (eventTitle != null) {
            findViewById<TextView>(R.id.matchTitle).text = eventTitle
        }
        
        if (eventVenue != null) {
            findViewById<TextView>(R.id.matchVenue).text = eventVenue
        }

        updateUI()
        updateEventsDisplay()
    }

    private fun createSampleLineup(matchId: String): MatchLineup {
        // TODO: Backend - Load actual lineup from backend
        // TODO: Backend - Implement lineup management and editing
        // TODO: Backend - Add lineup validation and conflict detection
        // TODO: Backend - Implement lineup templates and presets
        // TODO: Backend - Add lineup analytics and performance tracking
        // TODO: Backend - Implement lineup sharing and collaboration
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
            matchId = matchId,
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
        // TODO: Backend - Implement match start validation and permissions
        // TODO: Backend - Add match start notifications and alerts
        // TODO: Backend - Implement match start analytics and tracking
        // TODO: Backend - Add match start data synchronization
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
        // TODO: Backend - Implement match pause validation and permissions
        // TODO: Backend - Add match pause notifications and alerts
        // TODO: Backend - Implement match pause analytics and tracking
        // TODO: Backend - Add match pause data synchronization
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
        // TODO: Backend - Implement match end validation and permissions
        // TODO: Backend - Add match end notifications and alerts
        // TODO: Backend - Implement match end analytics and tracking
        // TODO: Backend - Add match end data synchronization
        // TODO: Backend - Implement post-match summary generation
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
        // TODO: Backend - Implement event undo validation and permissions
        // TODO: Backend - Add event undo notifications and alerts
        // TODO: Backend - Implement event undo analytics and tracking
        // TODO: Backend - Add event undo data synchronization
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
        // TODO: Backend - Implement timer control validation and permissions
        // TODO: Backend - Add timer control notifications and alerts
        // TODO: Backend - Implement timer control analytics and tracking
        // TODO: Backend - Add timer control data synchronization
        val timerSheet = TimerControlBottomSheet.newInstance(matchState.currentMinute)
        timerSheet.show(supportFragmentManager, "timer_control")
    }

    private fun showAnalytics() {
        // TODO: Backend - Show match analytics/statistics
        // TODO: Backend - Implement real-time analytics and reporting
        // TODO: Backend - Add analytics export and sharing
        // TODO: Backend - Implement analytics caching and offline access
        // TODO: Backend - Add analytics customization and filtering
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
        navigateBack()
    }

    private fun showRecordEventDialog() {
        // TODO: Backend - Show record event bottom sheet
        // TODO: Backend - Implement event recording validation and permissions
        // TODO: Backend - Add event recording notifications and alerts
        // TODO: Backend - Implement event recording analytics and tracking
        // TODO: Backend - Add event recording data synchronization
        val eventSheet = RecordEventBottomSheet.newInstance(matchState.matchId, matchState.currentMinute)
        eventSheet.show(supportFragmentManager, "record_event")
    }

    private fun showSettings() {
        // TODO: Backend - Show match settings
        // TODO: Backend - Implement match settings management
        // TODO: Backend - Add match settings validation and permissions
        // TODO: Backend - Implement match settings analytics and tracking
        // TODO: Backend - Add match settings data synchronization
        Snackbar.make(findViewById(android.R.id.content), "Settings coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showPostMatchSummary() {
        // TODO: Backend - Navigate to post-match summary screen
        // TODO: Backend - Implement post-match summary generation
        // TODO: Backend - Add post-match summary sharing and export
        // TODO: Backend - Implement post-match summary analytics and tracking
        // TODO: Backend - Add post-match summary data synchronization
        Snackbar.make(findViewById(android.R.id.content), "Match ended! Viewing post-match summary...", Snackbar.LENGTH_LONG).show()
        
        // Navigate to post-match summary
        // Intent(this, PostMatchSummaryActivity::class.java).apply {
        //     putExtra("match_id", matchState.matchId)
        //     startActivity(this)
        // }
    }

    // Enhanced Methods for Better Integration
    
    private fun updateEventsDisplay() {
        // TODO: Implement when events RecyclerView is added to layout
        // eventsAdapter.updateEvents(matchState.events)
        // 
        // // Scroll to latest event
        // if (matchState.events.isNotEmpty()) {
        //     eventsRecyclerView.scrollToPosition(0)
        // }
    }
    
    private fun showQuickActionsBottomSheet() {
        // TODO: Backend - Show quick actions bottom sheet
        val matchdaySheet = MatchdayBottomSheet.newInstance(matchState.matchId)
        matchdaySheet.show(supportFragmentManager, "matchday_actions")
    }
    
    private fun showScoreUpdateDialog(isHomeTeam: Boolean) {
        // TODO: Backend - Show score update dialog
        val teamName = if (isHomeTeam) matchState.homeTeam else matchState.awayTeam
        Snackbar.make(findViewById(android.R.id.content), 
            "Score update for $teamName coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showEventDetails(event: MatchEvent) {
        // TODO: Backend - Show event details dialog
        Snackbar.make(findViewById(android.R.id.content), 
            "Event: ${event.getEventDescription()}", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showEventActions(event: MatchEvent) {
        // TODO: Backend - Show event action dialog (edit/delete)
        AlertDialog.Builder(this)
            .setTitle("Event Actions")
            .setMessage("${event.getEventDescription()} at ${event.getFormattedTime()}")
            .setPositiveButton("Delete") { _, _ ->
                deleteEvent(event)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteEvent(event: MatchEvent) {
        // TODO: Backend - Delete event from backend
        matchState = matchState.copy(
            events = matchState.events.filter { it.id != event.id }
        )
        updateEventsDisplay()
        updateUI()
        
        Snackbar.make(findViewById(android.R.id.content), 
            "Event deleted", Snackbar.LENGTH_SHORT)
            .setAction("UNDO") {
                // Restore the event
                matchState = matchState.copy(
                    events = matchState.events + event
                )
                updateEventsDisplay()
                updateUI()
            }.show()
    }
    
    private fun navigateToPostMatch() {
        // TODO: Backend - Navigate to post-match results screen
        val intent = Intent(this, PostMatchActionsActivity::class.java).apply {
            putExtra("match_id", matchState.matchId)
            putExtra("home_team", matchState.homeTeam)
            putExtra("away_team", matchState.awayTeam)
            putExtra("home_score", matchState.homeScore)
            putExtra("away_score", matchState.awayScore)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        finish() // Close match control activity
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_match_control, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_formation -> {
                // TODO: View current formation
                Snackbar.make(findViewById(android.R.id.content), 
                    "Formation view coming soon", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_substitutions -> {
                // TODO: Show substitutions screen
                Snackbar.make(findViewById(android.R.id.content), 
                    "Substitutions coming soon", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_statistics -> {
                showAnalytics()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
} 
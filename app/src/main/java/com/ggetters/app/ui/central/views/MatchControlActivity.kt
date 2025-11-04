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
import com.ggetters.app.R
import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.MatchStatus
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.ui.central.adapters.MatchEventAdapter
import com.ggetters.app.ui.central.sheets.MatchdayBottomSheet
import com.ggetters.app.ui.central.sheets.RecordEventBottomSheet
import com.ggetters.app.ui.central.sheets.TimerControlBottomSheet
import com.ggetters.app.ui.central.viewmodels.MatchControlViewModel
import com.ggetters.app.ui.shared.extensions.getEventDescription
import com.ggetters.app.ui.shared.extensions.getFormattedTime
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.util.*

@AndroidEntryPoint
class MatchControlActivity : AppCompatActivity() {

    private val model: MatchControlViewModel by viewModels()

    private lateinit var matchDetails: MatchDetails
    private val events: MutableList<MatchEvent> = mutableListOf()

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
    private lateinit var btnRecordEvent: MaterialButton

    // Adapters
    private lateinit var eventsAdapter: MatchEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_control)

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
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        homeTeamName = findViewById(R.id.homeTeamName)
        awayTeamName = findViewById(R.id.awayTeamName)
        homeScore = findViewById(R.id.homeScore)
        awayScore = findViewById(R.id.awayScore)
        matchTimer = findViewById(R.id.matchTimer)
        matchStatus = findViewById(R.id.matchStatus)

        btnStartMatch = findViewById(R.id.btnStartMatch)
        btnPauseMatch = findViewById(R.id.btnPauseMatch)
        btnEndMatch = findViewById(R.id.btnEndMatch)
        btnRecordEvent = findViewById(R.id.btnRecordEvent)
    }

    private fun setupRecyclerView() {
        eventsAdapter = MatchEventAdapter(
            onEventClick = { showEventDetails(it) },
            onEventLongClick = { showEventActions(it) }
        )
        // TODO: assign RecyclerView from layout once added
        // recyclerView.layoutManager = LinearLayoutManager(this).apply { reverseLayout = true }
        // recyclerView.adapter = eventsAdapter
    }

    private fun setupClickListeners() {
        btnStartMatch.setOnClickListener { handleStartMatch() }
        btnPauseMatch.setOnClickListener { handlePauseMatch() }
        btnEndMatch.setOnClickListener { handleEndMatch() }
        btnRecordEvent.setOnClickListener { showRecordEventDialog() }
    }

    private fun loadMatchData() {
        val matchId = intent.getStringExtra("event_id") ?: UUID.randomUUID().toString()
        val opponent = intent.getStringExtra("event_opponent") ?: "Opponent"

        matchDetails = MatchDetails(
            matchId = matchId,
            title = intent.getStringExtra("event_title") ?: "Match",
            homeTeam = "Goal Getters FC",
            awayTeam = opponent,
            venue = intent.getStringExtra("event_venue") ?: "Stadium",
            date = Instant.now() , // this is wrong
            time = intent.getStringExtra("event_time") ?: "15:00",
            createdBy = "Coach"
        )
        updateUI()
    }

    private fun updateUI() {
        homeTeamName.text = matchDetails.homeTeam
        awayTeamName.text = matchDetails.awayTeam
        homeScore.text = matchDetails.homeScore.toString()
        awayScore.text = matchDetails.awayScore.toString()
        matchStatus.text = matchDetails.status.name
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (matchDetails.isMatchStarted()) updateUI()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun handleStartMatch() {
        matchDetails = matchDetails.copy(status = MatchStatus.IN_PROGRESS)
        events += MatchEvent(matchId = matchDetails.matchId, eventType = MatchEventType.MATCH_START, minute = 0, createdBy = "Coach")
        updateUI()
        Snackbar.make(findViewById(android.R.id.content), "Match started!", Snackbar.LENGTH_SHORT).show()
    }

    private fun handlePauseMatch() {
        matchDetails = matchDetails.copy(status = if (matchDetails.status == MatchStatus.IN_PROGRESS) MatchStatus.PAUSED else MatchStatus.IN_PROGRESS)
        updateUI()
    }

    private fun handleEndMatch() {
        matchDetails = matchDetails.copy(status = MatchStatus.FULL_TIME)
        events += MatchEvent(matchId = matchDetails.matchId, eventType = MatchEventType.MATCH_END, minute = 90, createdBy = "Coach")
        updateUI()
    }

    private fun showRecordEventDialog() {
        RecordEventBottomSheet.newInstance(matchDetails.matchId, "goal")
            .show(supportFragmentManager, "record_event")
    }

    private fun showEventDetails(event: MatchEvent) {
        Snackbar.make(findViewById(android.R.id.content), event.getEventDescription(), Snackbar.LENGTH_SHORT).show()
    }

    private fun showEventActions(event: MatchEvent) {
        AlertDialog.Builder(this)
            .setTitle("Event Actions")
            .setMessage("${event.getEventDescription()} at ${event.getFormattedTime()}")
            .setPositiveButton("Delete") { _, _ -> deleteEvent(event) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteEvent(event: MatchEvent) {
        events.removeIf { it.id == event.id }
        eventsAdapter.updateEvents(events)
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
}

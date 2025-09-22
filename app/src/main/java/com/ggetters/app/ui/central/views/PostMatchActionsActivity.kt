package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
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
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.PlayerMatchStats
import com.ggetters.app.data.model.MatchResult // doesnt exist
import com.ggetters.app.ui.central.adapters.PlayerStatsAdapter
import com.ggetters.app.ui.central.viewmodels.PostMatchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Implement match statistics calculation and analytics
// TODO: Backend - Add social sharing integration (WhatsApp, email, social media)
// TODO: Backend - Implement match report generation and export
// TODO: Backend - Add player performance ratings and feedback
// TODO: Backend - Implement team statistics and historical data
// TODO: Backend - Add match video/photo integration
// TODO: Backend - Implement match data synchronization with cloud

@AndroidEntryPoint
class PostMatchActionsActivity : AppCompatActivity() {

    private val viewModel: PostMatchViewModel by viewModels()
    
    // Match data
    private lateinit var matchResult: MatchResult
    private var matchId: String = ""
    private var homeTeam: String = ""
    private var awayTeam: String = ""
    private var homeScore: Int = 0
    private var awayScore: Int = 0

    // UI Elements
    private lateinit var homeTeamName: TextView
    private lateinit var awayTeamName: TextView
    private lateinit var finalHomeScore: TextView
    private lateinit var finalAwayScore: TextView
    private lateinit var matchResultText: TextView
    private lateinit var matchResultCard: MaterialCardView
    private lateinit var statsFilterChips: ChipGroup
    private lateinit var playerStatsRecyclerView: RecyclerView
    private lateinit var btnShareResults: MaterialButton
    private lateinit var btnSaveMatch: MaterialButton
    private lateinit var btnViewReport: MaterialButton
    private lateinit var btnBackToCalendar: MaterialButton

    // Adapters
    private lateinit var playerStatsAdapter: PlayerStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_match_actions)

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadMatchData()
        setupStatsFilters()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Match Results"
        }
    }

    private fun setupViews() {
        // Header back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Match result elements
        homeTeamName = findViewById(R.id.homeTeamName)
        awayTeamName = findViewById(R.id.awayTeamName)
        finalHomeScore = findViewById(R.id.finalHomeScore)
        finalAwayScore = findViewById(R.id.finalAwayScore)
        matchResultText = findViewById(R.id.matchResultText)
        matchResultCard = findViewById(R.id.matchResultCard)

        // Filter and stats
        statsFilterChips = findViewById(R.id.statsFilterChips)
        playerStatsRecyclerView = findViewById(R.id.playerStatsRecyclerView)

        // Action buttons
        btnShareResults = findViewById(R.id.btnShareResults)
        btnSaveMatch = findViewById(R.id.btnSaveMatch)
        btnViewReport = findViewById(R.id.btnViewReport)
        btnBackToCalendar = findViewById(R.id.btnBackToCalendar)
    }

    private fun setupRecyclerView() {
        playerStatsAdapter = PlayerStatsAdapter(
            onPlayerClick = { playerStats -> showPlayerDetails(playerStats) }
        )
        
        playerStatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostMatchActionsActivity)
            adapter = playerStatsAdapter
        }
    }

    private fun setupClickListeners() {
        btnShareResults.setOnClickListener {
            shareMatchResults()
        }

        btnSaveMatch.setOnClickListener {
            saveMatchData()
        }

        btnViewReport.setOnClickListener {
            viewMatchReport()
        }

        btnBackToCalendar.setOnClickListener {
            navigateBackToCalendar()
        }
    }

    private fun loadMatchData() {
        // Get match data from intent
        matchId = intent.getStringExtra("match_id") ?: ""
        homeTeam = intent.getStringExtra("home_team") ?: "Home Team"
        awayTeam = intent.getStringExtra("away_team") ?: "Away Team"
        homeScore = intent.getIntExtra("home_score", 0)
        awayScore = intent.getIntExtra("away_score", 0)

        // Create match result
        matchResult = createMatchResult()

        // Update UI
        updateMatchResultUI()
        loadPlayerStatistics()
    }

    private fun createMatchResult(): MatchResult {
        // TODO: Backend - Load actual match result from backend
        return MatchResult(
            matchId = matchId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            matchDate = System.currentTimeMillis(),
            duration = 90, // minutes
            goals = createSampleGoals(),
            cards = createSampleCards(),
            substitutions = createSampleSubstitutions(),
            possession = mapOf("home" to 55, "away" to 45),
            shots = mapOf("home" to 12, "away" to 8),
            shotsOnTarget = mapOf("home" to 6, "away" to 3),
            corners = mapOf("home" to 7, "away" to 4),
            fouls = mapOf("home" to 11, "away" to 14)
        )
    }

    private fun createSampleGoals(): List<MatchEvent> {
        // TODO: Backend - Load actual goals from match events
        return listOf(
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.GOAL,
                minute = 23,
                playerId = "player1",
                playerName = "Luke Jackson",
                teamId = "home",
                details = mapOf("goalType" to "Header", "assist" to "Mike Wilson"),
                createdBy = "coach1"
            ),
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.GOAL,
                minute = 67,
                playerId = "player2",
                playerName = "David Smith",
                teamId = "home",
                details = mapOf("goalType" to "Left foot", "assist" to "None"),
                createdBy = "coach1"
            )
        )
    }

    private fun createSampleCards(): List<MatchEvent> {
        // TODO: Backend - Load actual cards from match events
        return listOf(
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.YELLOW_CARD,
                minute = 34,
                playerId = "player3",
                playerName = "Tom Brown",
                teamId = "away",
                details = mapOf("reason" to "Unsporting behavior"),
                createdBy = "coach1"
            )
        )
    }

    private fun createSampleSubstitutions(): List<MatchEvent> {
        // TODO: Backend - Load actual substitutions from match events
        return listOf(
            MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.SUBSTITUTION,
                minute = 75,
                playerId = "player4",
                playerName = "Fresh Player",
                teamId = "home",
                details = mapOf("playerOut" to "Tired Player", "playerIn" to "Fresh Player"),
                createdBy = "coach1"
            )
        )
    }

    private fun updateMatchResultUI() {
        homeTeamName.text = homeTeam
        awayTeamName.text = awayTeam
        finalHomeScore.text = homeScore.toString()
        finalAwayScore.text = awayScore.toString()

        // Determine match result
        val resultText = when {
            homeScore > awayScore -> "Victory!"
            homeScore < awayScore -> "Defeat"
            else -> "Draw"
        }
        matchResultText.text = resultText

        // Update card styling based on result
        val cardColor = when {
            homeScore > awayScore -> getColor(R.color.success_light)
            homeScore < awayScore -> getColor(R.color.error_light)
            else -> getColor(R.color.warning_light)
        }
        matchResultCard.setCardBackgroundColor(cardColor)
    }

    private fun setupStatsFilters() {
        val filters = listOf("All Players", "Goals", "Assists", "Cards", "Substitutions")
        
        filters.forEach { filter ->
            val chip = Chip(this).apply {
                text = filter
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        filterPlayerStats(filter)
                        // Uncheck other chips
                        for (i in 0 until statsFilterChips.childCount) {
                            val otherChip = statsFilterChips.getChildAt(i) as Chip
                            if (otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                    }
                }
            }
            
            if (filter == "All Players") {
                chip.isChecked = true
            }
            
            statsFilterChips.addView(chip)
        }
    }

    private fun loadPlayerStatistics() {
        // TODO: Backend - Load actual player statistics
        val playerStats = createSamplePlayerStats()
        playerStatsAdapter.updateStats(playerStats)
    }

    private fun createSamplePlayerStats(): List<PlayerMatchStats> {
        // TODO: Backend - Calculate actual player statistics
        return listOf(
            PlayerMatchStats(
                playerId = "player1",
                playerName = "Luke Jackson",
                jerseyNumber = 10,
                position = "FW",
                minutesPlayed = 90,
                goals = 1,
                assists = 0,
                shots = 4,
                shotsOnTarget = 2,
                passes = 45,
                passAccuracy = 89,
                tackles = 2,
                interceptions = 1,
                fouls = 1,
                yellowCards = 0,
                redCards = 0,
                rating = 8.5
            ),
            PlayerMatchStats(
                playerId = "player2",
                playerName = "David Smith",
                jerseyNumber = 9,
                position = "FW",
                minutesPlayed = 90,
                goals = 1,
                assists = 1,
                shots = 3,
                shotsOnTarget = 2,
                passes = 38,
                passAccuracy = 92,
                tackles = 1,
                interceptions = 0,
                fouls = 0,
                yellowCards = 0,
                redCards = 0,
                rating = 8.0
            ),
            PlayerMatchStats(
                playerId = "player3",
                playerName = "Mike Wilson",
                jerseyNumber = 8,
                position = "MF",
                minutesPlayed = 90,
                goals = 0,
                assists = 1,
                shots = 2,
                shotsOnTarget = 1,
                passes = 67,
                passAccuracy = 94,
                tackles = 4,
                interceptions = 3,
                fouls = 2,
                yellowCards = 0,
                redCards = 0,
                rating = 7.5
            )
        )
    }

    private fun filterPlayerStats(filter: String) {
        // TODO: Backend - Implement proper filtering logic
        val allStats = createSamplePlayerStats()
        val filteredStats = when (filter) {
            "Goals" -> allStats.filter { it.goals > 0 }
            "Assists" -> allStats.filter { it.assists > 0 }
            "Cards" -> allStats.filter { it.yellowCards > 0 || it.redCards > 0 }
            "Substitutions" -> allStats.filter { it.minutesPlayed < 90 }
            else -> allStats
        }
        playerStatsAdapter.updateStats(filteredStats)
    }

    private fun showPlayerDetails(playerStats: PlayerMatchStats) {
        // TODO: Backend - Show detailed player statistics dialog
        Snackbar.make(findViewById(android.R.id.content),
            "${playerStats.playerName}: ${playerStats.goals} goals, ${playerStats.assists} assists, Rating: ${playerStats.rating}",
            Snackbar.LENGTH_LONG).show()
    }

    private fun shareMatchResults() {
        // TODO: Backend - Implement social sharing
        val shareText = buildShareText()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Match Result: $homeTeam vs $awayTeam")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Match Results"))
    }

    private fun buildShareText(): String {
        return """
            🏆 MATCH RESULT 🏆
            
            $homeTeam $homeScore - $awayScore $awayTeam
            
            ⚽ Goals:
            ${matchResult.goals.joinToString("\n") { "${it.minute}' ${it.playerName}" }}
            
            📊 Match Stats:
            Possession: ${matchResult.possession["home"]}% - ${matchResult.possession["away"]}%
            Shots: ${matchResult.shots["home"]} - ${matchResult.shots["away"]}
            Corners: ${matchResult.corners["home"]} - ${matchResult.corners["away"]}
            
            Shared via Goal Getters FC App
        """.trimIndent()
    }

    private fun saveMatchData() {
        // TODO: Backend - Save match data to backend
        Snackbar.make(findViewById(android.R.id.content),
            "Match data saved successfully!", Snackbar.LENGTH_SHORT).show()
    }

    private fun viewMatchReport() {
        // TODO: Backend - Generate and view detailed match report
        Snackbar.make(findViewById(android.R.id.content),
            "Match report generation coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateBackToCalendar() {
        // Return to calendar/home screen
        finish()
        // TODO: Backend - Navigate specifically to calendar with match updated
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_post_match, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportMatchData()
                true
            }
            R.id.action_edit_result -> {
                editMatchResult()
                true
            }
            R.id.action_add_notes -> {
                addMatchNotes()
                true
            }
            android.R.id.home -> {
                finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportMatchData() {
        // TODO: Backend - Export match data to CSV/PDF
        Snackbar.make(findViewById(android.R.id.content),
            "Export functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun editMatchResult() {
        // TODO: Backend - Allow editing of match result
        Snackbar.make(findViewById(android.R.id.content),
            "Edit result functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun addMatchNotes() {
        // TODO: Backend - Add match notes and comments
        Snackbar.make(findViewById(android.R.id.content),
            "Match notes functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
}

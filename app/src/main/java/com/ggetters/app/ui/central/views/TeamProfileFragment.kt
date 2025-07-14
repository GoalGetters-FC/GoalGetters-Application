package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.data.model.Team
import com.ggetters.app.ui.central.models.TeamStats

class TeamProfileFragment : Fragment() {
    
    private lateinit var teamNameText: TextView
    private lateinit var teamStatsText: TextView
    private lateinit var teamInfoText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_team_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        loadTeamProfile()
    }
    
    private fun setupViews(view: View) {
        teamNameText = view.findViewById(R.id.teamNameText)
        teamStatsText = view.findViewById(R.id.teamStatsText)
        teamInfoText = view.findViewById(R.id.teamInfoText)
    }
    
    private fun loadTeamProfile() {
        // TODO: Backend - Fetch team profile from API
        // Endpoint: GET /api/teams/{teamId}/profile
        // Request: { teamId: String }
        // Response: { team: Team, stats: TeamStats, info: TeamInfo }
        // Error handling: { message: String, code: String }
        
        // Sample data for now
        val sampleTeam = Team(
            name = "Goal Getters FC"
        )
        
        val sampleStats = TeamStats(
            totalMatches = 45,
            wins = 28,
            draws = 8,
            losses = 9,
            goalsScored = 89,
            goalsConceded = 42,
            points = 92
        )
        
        displayTeamInfo(sampleTeam, sampleStats)
    }
    
    private fun displayTeamInfo(team: Team, stats: TeamStats) {
        teamNameText.text = team.name
        teamInfoText.text = """
            Founded: 2020
            Home Ground: Main Stadium
            Coach: Coach Smith
            Captain: John Doe
        """.trimIndent()
        
        teamStatsText.text = """
            Matches: ${stats.totalMatches}
            Wins: ${stats.wins}
            Draws: ${stats.draws}
            Losses: ${stats.losses}
            Goals Scored: ${stats.goalsScored}
            Goals Conceded: ${stats.goalsConceded}
            Points: ${stats.points}
        """.trimIndent()
    }
} 
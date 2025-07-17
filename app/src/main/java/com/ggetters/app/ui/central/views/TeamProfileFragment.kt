package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.TeamStats

/**
 * Lightweight model just for this demo screen.
 * Replace with your real Team entity + repository later.
 */
private data class ViewTeam(
    val name: String,
    val founded: String,
    val homeGround: String,
    val coach: String,
    val captain: String
)

class TeamProfileFragment : Fragment() {

    private lateinit var teamNameText: TextView
    private lateinit var teamStatsText: TextView
    private lateinit var teamInfoText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        loadTeamProfile()
    }

    private fun setupViews(view: View) {
        teamNameText  = view.findViewById(R.id.teamNameText)
        teamStatsText = view.findViewById(R.id.teamStatsText)
        teamInfoText  = view.findViewById(R.id.teamInfoText)
    }

    private fun loadTeamProfile() {
        // TODO: Replace with real TeamRepository call:
        // val team = teamRepo.getById(currentTeamId)
        // val stats = teamStatsRepo.getStatsFor(team.id)

        // Sample/demo data:
        val sampleTeam = ViewTeam(
            name       = "Goal Getters FC",
            founded    = "2020",
            homeGround = "Main Stadium",
            coach      = "Coach Smith",
            captain    = "John Doe"
        )

        val sampleStats = TeamStats(
            totalMatches  = 45,
            wins           = 28,
            draws          = 8,
            losses         = 9,
            goalsScored    = 89,
            goalsConceded  = 42,
            points         = 92
        )

        displayTeamInfo(sampleTeam, sampleStats)
    }

    private fun displayTeamInfo(team: ViewTeam, stats: TeamStats) {
        teamNameText.text = team.name

        teamInfoText.text = """
      Founded: ${team.founded}
      Home Ground: ${team.homeGround}
      Coach: ${team.coach}
      Captain: ${team.captain}
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

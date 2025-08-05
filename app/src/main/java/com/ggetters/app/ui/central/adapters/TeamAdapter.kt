package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Team
import com.ggetters.app.ui.central.models.TeamComposition
import com.google.android.material.button.MaterialButton

class TeamAdapter(
    private val onTeamClick: (Team) -> Unit,
    private val onSwitchTeam: (Team) -> Unit,
    private val onDeleteTeam: (Team) -> Unit
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    private var teams: List<Team> = emptyList()

    fun updateTeams(newTeams: List<Team>) {
        teams = newTeams
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(teams[position])
    }

    override fun getItemCount(): Int = teams.size

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamIcon: ImageView = itemView.findViewById(R.id.teamIcon)
        private val teamName: TextView = itemView.findViewById(R.id.teamName)
        private val teamRole: TextView = itemView.findViewById(R.id.teamRole)
        private val memberCount: TextView = itemView.findViewById(R.id.memberCount)
        private val currentTeamBadge: TextView = itemView.findViewById(R.id.currentTeamBadge)
        private val switchTeamButton: MaterialButton = itemView.findViewById(R.id.switchTeamButton)
        private val deleteTeamButton: MaterialButton = itemView.findViewById(R.id.deleteTeamButton)

        fun bind(team: Team) {
            teamName.text = team.name
            teamRole.text = team.role
            memberCount.text = "${team.memberCount} members"

            // Show current team badge
            if (team.isCurrentTeam) {
                currentTeamBadge.visibility = View.VISIBLE
                currentTeamBadge.text = "Current Team"
            } else {
                currentTeamBadge.visibility = View.GONE
            }

            // Show/hide action buttons based on current team status
            if (team.isCurrentTeam) {
                switchTeamButton.visibility = View.GONE
                deleteTeamButton.visibility = View.GONE
            } else {
                switchTeamButton.visibility = View.VISIBLE
                deleteTeamButton.visibility = View.VISIBLE
            }

            // Set team icon based on composition
            when (team.composition) {
                TeamComposition.UNISEX_MALE -> teamIcon.setImageResource(R.drawable.ic_unicons_soccer_24)
                TeamComposition.UNISEX_FEMALE -> teamIcon.setImageResource(R.drawable.ic_unicons_soccer_24)
                TeamComposition.MALE_ONLY -> teamIcon.setImageResource(R.drawable.ic_unicons_soccer_24)
                TeamComposition.FEMALE_ONLY -> teamIcon.setImageResource(R.drawable.ic_unicons_soccer_24)
            }

            // Click listeners
            itemView.setOnClickListener {
                onTeamClick(team)
            }

            switchTeamButton.setOnClickListener {
                onSwitchTeam(team)
            }

            deleteTeamButton.setOnClickListener {
                onDeleteTeam(team)
            }
        }
    }
} 
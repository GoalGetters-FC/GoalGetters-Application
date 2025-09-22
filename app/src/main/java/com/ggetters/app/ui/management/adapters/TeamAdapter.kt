package com.ggetters.app.ui.management.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.management.models.Team

class TeamAdapter(
    private val onTeamClick: (Team) -> Unit,
    private val onSwitchTeam: (Team) -> Unit,
    private val onDeleteTeam: (Team) -> Unit
) : ListAdapter<Team, TeamAdapter.TeamViewHolder>(TeamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view, onTeamClick, onSwitchTeam, onDeleteTeam)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TeamViewHolder(
        itemView: View,
        private val onTeamClick: (Team) -> Unit,
        private val onSwitchTeam: (Team) -> Unit,
        private val onDeleteTeam: (Team) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val teamAvatar: ImageView = itemView.findViewById(R.id.teamAvatar)
        private val teamName: TextView = itemView.findViewById(R.id.teamName)
        private val teamRole: TextView = itemView.findViewById(R.id.teamRole)
        private val memberCount: TextView = itemView.findViewById(R.id.memberCount)
        private val coachCount: TextView = itemView.findViewById(R.id.coachCount)
        private val playerCount: TextView = itemView.findViewById(R.id.playerCount)
        private val guardianCount: TextView = itemView.findViewById(R.id.guardianCount)
        private val currentTeamBadge: TextView = itemView.findViewById(R.id.currentTeamBadge)
        private val teamMenuButton: ImageView = itemView.findViewById(R.id.teamMenuButton)

        fun bind(team: Team) {
            teamName.text = team.name
            teamRole.text = team.role
            
            // Set member counts (TODO: Get real data from backend)
            memberCount.text = "${team.memberCount} members"
            coachCount.text = "0 coaches" // TODO: Get coach count from backend
            playerCount.text = "0 players" // TODO: Get player count from backend
            guardianCount.text = "0 guardians" // TODO: Get guardian count from backend
            
            // Show current team badge if this is the active team
            currentTeamBadge.visibility = if (team.isCurrentTeam) View.VISIBLE else View.GONE
            
            // Setup click listeners
            itemView.setOnClickListener {
                onTeamClick(team)
            }
            
            teamMenuButton.setOnClickListener { view ->
                showTeamMenu(view, team)
            }
        }
        
        private fun showTeamMenu(view: View, team: Team) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_team_actions, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_switch_team -> {
                        onSwitchTeam(team)
                        true
                    }
                    R.id.action_delete_team -> {
                        onDeleteTeam(team)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }

    private class TeamDiffCallback : DiffUtil.ItemCallback<Team>() {
        override fun areItemsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem == newItem
        }
    }
} 
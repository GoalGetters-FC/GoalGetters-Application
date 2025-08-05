package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.UserAccount
import com.google.android.material.button.MaterialButton

class TeamAdapter(
    private val onTeamClick: (UserAccount) -> Unit,
    private val onSetDefaultClick: (UserAccount) -> Unit
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {
    
    private var teams: List<UserAccount> = emptyList()
    
    fun updateTeams(newTeams: List<UserAccount>) {
        teams = newTeams
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_viewer_account, parent, false)
        return TeamViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(teams[position])
    }
    
    override fun getItemCount(): Int = teams.size
    
    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamLogo: ImageView = itemView.findViewById(R.id.teamLogo)
        private val teamName: TextView = itemView.findViewById(R.id.tv_team_name)
        private val teamRole: TextView = itemView.findViewById(R.id.tv_team_role)
        private val teamCount: TextView = itemView.findViewById(R.id.tv_team_count)
        private val optionsButton: ImageView = itemView.findViewById(R.id.iv_options)
        private val defaultButton: MaterialButton = itemView.findViewById(R.id.btnSetDefault)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTeamClick(teams[position])
                }
            }
            
            defaultButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSetDefaultClick(teams[position])
                }
            }
        }
        
        fun bind(team: UserAccount) {
            teamName.text = team.teamName
            teamRole.text = team.role
            
            // Show/hide default button based on active status
            if (team.isActive) {
                defaultButton.text = "Default"
                defaultButton.isEnabled = false
            } else {
                defaultButton.text = "Set Default"
                defaultButton.isEnabled = true
            }
            
            // Set team logo (placeholder for now)
            teamLogo.setImageResource(R.drawable.ic_unicons_soccer_24)
            
            // TODO: Load team logo with Glide/Coil
            // Glide.with(itemView.context).load(team.avatar).into(teamLogo)
        }
    }
} 
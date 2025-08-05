package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Player

class PlayerAdapter(
    private val onPlayerClick: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
    
    private var players: List<Player> = emptyList()
    
    fun updatePlayers(newPlayers: List<Player>) {
        players = newPlayers
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }
    
    override fun getItemCount(): Int = players.size
    
    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerAvatar: ImageView? = itemView.findViewById(R.id.playerAvatar)
        private val playerName: TextView? = itemView.findViewById(R.id.playerName)
        private val chipPosition: TextView? = itemView.findViewById(R.id.chipPosition)
        private val playerStats: TextView? = itemView.findViewById(R.id.playerStats)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlayerClick(players[position])
                }
            }
        }
        
        fun bind(player: Player) {
            playerName?.text = player.getFullName()
            chipPosition?.text = player.position
            playerStats?.text = "Sessions: ${player.stats?.matches ?: 0}/18"
            
            // Set player avatar (placeholder for now)
            playerAvatar?.setImageResource(R.drawable.ic_unicons_user_24)
            
            // TODO: Backend - Load player avatar with Glide/Coil
            // Glide.with(itemView.context).load(player.avatar).into(playerAvatar)
        }
    }
} 
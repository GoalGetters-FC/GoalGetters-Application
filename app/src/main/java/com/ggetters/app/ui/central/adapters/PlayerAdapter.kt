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
    private val onPlayerClick: (Player) -> Unit,
    private val onPlayerLongClick: (Player) -> Unit
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
        private val avatarImage: ImageView = itemView.findViewById(R.id.playerAvatar)
        private val nameText: TextView = itemView.findViewById(R.id.playerName)
        private val positionText: TextView = itemView.findViewById(R.id.playerPosition)
        private val jerseyNumberText: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val statsText: TextView = itemView.findViewById(R.id.playerStats)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlayerClick(players[position])
                }
            }
            
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlayerLongClick(players[position])
                }
                true
            }
        }
        
        fun bind(player: Player) {
            nameText.text = player.name
            positionText.text = player.position
            jerseyNumberText.text = "#${player.jerseyNumber}"
            
            // Display stats
            statsText.text = "${player.stats.goals}G ${player.stats.assists}A (${player.stats.matches}M)"
            
            // Set status indicator
            statusIndicator.setBackgroundResource(
                if (player.isActive) R.color.success_green else R.color.text_disabled
            )
            
            // TODO: Load player avatar using Glide or similar
            // if (player.avatar != null) {
            //     Glide.with(itemView.context)
            //         .load(player.avatar)
            //         .placeholder(R.drawable.default_avatar)
            //         .into(avatarImage)
            // } else {
            //     avatarImage.setImageResource(R.drawable.default_avatar)
            // }
        }
    }
} 
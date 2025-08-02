package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Player
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
        private val avatarBorder: ImageView = itemView.findViewById(R.id.avatarBorder)
        private val nameText: TextView = itemView.findViewById(R.id.playerName)
        private val chipPosition: Chip = itemView.findViewById(R.id.chipPosition)
        private val statsText: TextView = itemView.findViewById(R.id.playerStats)
        private val chipCaptain: Chip = itemView.findViewById(R.id.chipCaptain)
        private val chipNew: Chip = itemView.findViewById(R.id.chipNew)
        private val chipInjured: Chip = itemView.findViewById(R.id.chipInjured)
        private val roleIcon: ImageView = itemView.findViewById(R.id.roleIcon)
        private val chevronIcon: ImageView = itemView.findViewById(R.id.chevronIcon)
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
            chipPosition.text = player.position
            statsText.text = "Sessions: ${player.stats.matches}"
            // Show/hide status chips
            chipCaptain.visibility = if (player.position.equals("Captain", true)) View.VISIBLE else View.GONE
            chipNew.visibility = if (player.position.equals("New", true)) View.VISIBLE else View.GONE
            chipInjured.visibility = if (player.position.equals("Injured", true)) View.VISIBLE else View.GONE
            // Show blue border if active
            avatarBorder.visibility = if (player.isActive) View.VISIBLE else View.GONE
            // Set role icon
            when (player.position.lowercase()) {
                "coach" -> roleIcon.setImageResource(R.drawable.ic_unicons_hat_24)
                "assistant" -> roleIcon.setImageResource(R.drawable.ic_unicons_handshake_24)
                "goalkeeper", "gk" -> roleIcon.setImageResource(R.drawable.ic_unicons_shield_24)
                "guardian" -> roleIcon.setImageResource(R.drawable.ic_unicons_user_plus_16)
                else -> roleIcon.setImageResource(R.drawable.ic_unicons_user_24)
            }
            chevronIcon.setImageResource(R.drawable.ic_unicons_arrow_right_24)
            // TODO: Load player avatar using Glide or similar
        }
    }
} 
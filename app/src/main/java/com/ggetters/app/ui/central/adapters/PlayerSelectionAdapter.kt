package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.LineupPlayer

class PlayerSelectionAdapter(
    private val players: List<LineupPlayer>,
    private val onPlayerSelected: (LineupPlayer) -> Unit
) : RecyclerView.Adapter<PlayerSelectionAdapter.PlayerViewHolder>() {

    private var selectedPlayer: LineupPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_selection, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val playerPosition: TextView = itemView.findViewById(R.id.playerPosition)
        private val playerNumber: TextView = itemView.findViewById(R.id.playerNumber)

        fun bind(player: LineupPlayer) {
            playerName.text = player.playerName
            playerPosition.text = player.position
            playerNumber.text = player.jerseyNumber.toString()

            // Set selection state
            val isSelected = selectedPlayer?.playerId == player.playerId
            itemView.isSelected = isSelected

            // Set background based on selection
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.player_selection_selected)
            } else {
                itemView.setBackgroundResource(R.drawable.player_selection_normal)
            }

            // Set click listener
            itemView.setOnClickListener {
                selectedPlayer = player
                onPlayerSelected(player)
                notifyDataSetChanged() // Update all items to show selection
            }
        }
    }
} 
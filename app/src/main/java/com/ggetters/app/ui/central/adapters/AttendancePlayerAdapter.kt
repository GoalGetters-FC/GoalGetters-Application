package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.PlayerAvailability
import com.ggetters.app.ui.central.models.RSVPStatus

class AttendancePlayerAdapter(
    private val onPlayerAction: (PlayerAvailability, String) -> Unit
) : RecyclerView.Adapter<AttendancePlayerAdapter.PlayerViewHolder>() {

    private var players = listOf<PlayerAvailability>()

    fun updatePlayers(newPlayers: List<PlayerAvailability>) {
        val diffCallback = PlayerDiffCallback(players, newPlayers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        players = newPlayers
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)
        private val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        private val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)

        fun bind(player: PlayerAvailability) {
            // Player name
            playerName.text = player.playerName
            
            // Jersey number
            jerseyNumber.text = player.jerseyNumber.toString()
            
            // Player avatar (placeholder for now)
            playerAvatar.setImageResource(R.drawable.ic_unicons_user_24)
            
            // Status indicator color
            val statusColor = when (player.status) {
                RSVPStatus.AVAILABLE -> ContextCompat.getColor(itemView.context, R.color.success)
                RSVPStatus.MAYBE -> ContextCompat.getColor(itemView.context, R.color.warning)
                RSVPStatus.UNAVAILABLE -> ContextCompat.getColor(itemView.context, R.color.error)
                RSVPStatus.NOT_RESPONDED -> ContextCompat.getColor(itemView.context, R.color.outline)
            }
            statusIndicator.setBackgroundColor(statusColor)
            
            // Menu button click listener
            menuButton.setOnClickListener {
                onPlayerAction(player, "menu")
            }
            
            // Status indicator click to change status
            statusIndicator.setOnClickListener {
                onPlayerAction(player, "status_change")
            }
            
            // Player name click for quick status change
            playerName.setOnClickListener {
                onPlayerAction(player, "status_change")
            }
            
            // Set row styling based on status
            when (player.status) {
                RSVPStatus.AVAILABLE -> {
                    itemView.alpha = 1.0f
                    playerName.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface))
                }
                RSVPStatus.MAYBE -> {
                    itemView.alpha = 0.8f
                    playerName.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface))
                }
                RSVPStatus.UNAVAILABLE -> {
                    itemView.alpha = 0.5f
                    playerName.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface_variant))
                }
                RSVPStatus.NOT_RESPONDED -> {
                    itemView.alpha = 0.7f
                    playerName.setTextColor(ContextCompat.getColor(itemView.context, R.color.on_surface_variant))
                }
            }
        }
    }

    private class PlayerDiffCallback(
        private val oldList: List<PlayerAvailability>,
        private val newList: List<PlayerAvailability>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].playerId == newList[newItemPosition].playerId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

package com.ggetters.app.ui.central.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.PlayerAvailability
import com.ggetters.app.ui.central.models.RSVPStatus
import com.google.android.material.card.MaterialCardView

class FormationPlayerAdapter(
    private val onPlayerDragStart: (PlayerAvailability) -> Unit,
    private val onPlayerClick: (PlayerAvailability) -> Unit
) : RecyclerView.Adapter<FormationPlayerAdapter.PlayerViewHolder>() {

    private var players = listOf<PlayerAvailability>()

    fun updatePlayers(newPlayers: List<PlayerAvailability>) {
        val diffCallback = PlayerDiffCallback(players, newPlayers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        players = newPlayers
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_formation_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerCard: MaterialCardView = itemView.findViewById(R.id.playerCard)
        private val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val playerPosition: TextView = itemView.findViewById(R.id.playerPosition)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(player: PlayerAvailability) {
            jerseyNumber.text = player.jerseyNumber.toString()
            playerName.text = player.playerName.split(" ").let { 
                if (it.size > 1) "${it[0]} ${it.last()[0]}." else it[0] 
            }
            playerPosition.text = player.position
            
            updateStatusIndicator(player)
            updateCardStyling(player)
            setupDragAndDrop(player)
            
            // Click listener
            playerCard.setOnClickListener { onPlayerClick(player) }
        }

        private fun updateStatusIndicator(player: PlayerAvailability) {
            val color = when (player.status) {
                RSVPStatus.AVAILABLE -> ContextCompat.getColor(itemView.context, R.color.success)
                RSVPStatus.MAYBE -> ContextCompat.getColor(itemView.context, R.color.warning)
                RSVPStatus.UNAVAILABLE -> ContextCompat.getColor(itemView.context, R.color.error)
                RSVPStatus.NOT_RESPONDED -> ContextCompat.getColor(itemView.context, R.color.text_tertiary)
            }
            
            statusIndicator.setBackgroundColor(color)
        }

        private fun updateCardStyling(player: PlayerAvailability) {
            val cardBackground = when (player.status) {
                RSVPStatus.AVAILABLE -> ContextCompat.getColor(itemView.context, R.color.surface_container)
                RSVPStatus.MAYBE -> ContextCompat.getColor(itemView.context, R.color.warning_light)
                RSVPStatus.UNAVAILABLE -> ContextCompat.getColor(itemView.context, R.color.error_light)
                RSVPStatus.NOT_RESPONDED -> ContextCompat.getColor(itemView.context, R.color.surface_variant)
            }
            
            playerCard.setCardBackgroundColor(cardBackground)
            
            val strokeColor = when (player.status) {
                RSVPStatus.AVAILABLE -> ContextCompat.getColor(itemView.context, R.color.success)
                RSVPStatus.MAYBE -> ContextCompat.getColor(itemView.context, R.color.warning)
                RSVPStatus.UNAVAILABLE -> ContextCompat.getColor(itemView.context, R.color.error)
                RSVPStatus.NOT_RESPONDED -> ContextCompat.getColor(itemView.context, R.color.outline)
            }
            
            playerCard.strokeColor = strokeColor
            playerCard.strokeWidth = 2
            
            // Disable interaction for unavailable players
            playerCard.isEnabled = player.status != RSVPStatus.UNAVAILABLE
            playerCard.alpha = if (player.status == RSVPStatus.UNAVAILABLE) 0.6f else 1.0f
        }

        private fun setupDragAndDrop(player: PlayerAvailability) {
            if (player.status == RSVPStatus.UNAVAILABLE) return
            
            playerCard.setOnLongClickListener { view ->
                onPlayerDragStart(player)
                
                val item = ClipData.Item(player.playerId)
                val dragData = ClipData(
                    "Player",
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )
                
                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDragAndDrop(dragData, shadowBuilder, player, 0)
                
                // Hide the original view during drag
                view.alpha = 0.5f
                
                true
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

package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.PlayerAvailability

class LineupPlayerGridAdapter(
    private val onPlayerClick: (PlayerAvailability) -> Unit,
    private val onAddPlayerClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var players = listOf<PlayerAvailability>()
    private val VIEW_TYPE_PLAYER = 0
    private val VIEW_TYPE_ADD_BUTTON = 1

    fun updatePlayers(newPlayers: List<PlayerAvailability>) {
        val diffCallback = PlayerDiffCallback(players, newPlayers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        players = newPlayers
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < players.size) VIEW_TYPE_PLAYER else VIEW_TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLAYER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_lineup_player_grid, parent, false)
                PlayerViewHolder(view)
            }
            VIEW_TYPE_ADD_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_player_button, parent, false)
                AddPlayerViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlayerViewHolder -> holder.bind(players[position])
            is AddPlayerViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = players.size + 1 // +1 for add button

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)
        private val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val playerPosition: TextView = itemView.findViewById(R.id.playerPosition)

        fun bind(player: PlayerAvailability) {
            // Player avatar (using placeholder)
            playerAvatar.setImageResource(R.drawable.ic_unicons_user_24)
            
            // Jersey number
            jerseyNumber.text = player.jerseyNumber.toString()
            
            // Player name
            playerName.text = player.playerName
            
            // Player position
            playerPosition.text = player.position
            
            // Click listener
            itemView.setOnClickListener {
                onPlayerClick(player)
            }
            
            // Styling based on availability (could be used for visual feedback)
            when (player.status) {
                com.ggetters.app.ui.central.models.RSVPStatus.AVAILABLE -> {
                    itemView.alpha = 1.0f
                }
                else -> {
                    itemView.alpha = 0.7f
                }
            }
        }
    }

    inner class AddPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addButton: ImageView = itemView.findViewById(R.id.addPlayerIcon)

        fun bind() {
            addButton.setImageResource(R.drawable.ic_unicons_plus_24)
            addButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.on_surface_variant))
            
            itemView.setOnClickListener {
                onAddPlayerClick()
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

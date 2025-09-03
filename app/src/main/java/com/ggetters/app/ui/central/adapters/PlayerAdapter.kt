package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.ui.shared.extensions.getFullName

class PlayerAdapter(
    private val onPlayerClick: (User) -> Unit,
    private val onPlayerLongPress: (User) -> Unit = {}
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    private var players: List<User> = emptyList()

    fun updatePlayers(newPlayers: List<User>) {
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
        private val playerAvatar: ImageView = itemView.findViewById(R.id.playerAvatar)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val chipPosition: TextView = itemView.findViewById(R.id.chipPosition)
        private val playerStats: TextView = itemView.findViewById(R.id.playerStats)

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
                    onPlayerLongPress(players[position])
                    true
                } else {
                    false
                }
            }
        }

        fun bind(user: User) {
            playerName.text = user.fullName()
            chipPosition.text = user.position?.name ?: "N/A"

            // Jersey number if available
            //jerseyNumber.text = user.number?.toString() ?: "-"

            // Remove or show placeholder stats since User doesn't contain stats
            playerStats.text = "No stats yet"

            // Placeholder avatar
            playerAvatar.setImageResource(R.drawable.ic_unicons_user_24)
        }

    }
}

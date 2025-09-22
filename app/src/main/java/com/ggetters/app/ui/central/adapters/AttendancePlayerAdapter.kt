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
import com.ggetters.app.data.model.AttendanceWithUser

class AttendancePlayerAdapter(
    private val onPlayerAction: (AttendanceWithUser, String, View?) -> Unit
) : RecyclerView.Adapter<AttendancePlayerAdapter.PlayerViewHolder>() {

    private var players = listOf<AttendanceWithUser>()

    fun updatePlayers(newPlayers: List<AttendanceWithUser>) {
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

        fun bind(player: AttendanceWithUser) {
            val (attendance, user) = player

            // Player name & jersey
            playerName.text = user.fullName()
            jerseyNumber.text = user.number?.toString() ?: "-"

            // Avatar placeholder
            playerAvatar.setImageResource(R.drawable.ic_unicons_user_24)

            // Status color - 0=Present,1=Absent,2=Late,3=Excused
            val statusColor = when (attendance.status) {
                0 -> ContextCompat.getColor(itemView.context, R.color.success) // Present (green)
                1 -> ContextCompat.getColor(itemView.context, R.color.error)   // Absent (red)
                2 -> ContextCompat.getColor(itemView.context, R.color.warning) // Late (orange)
                3 -> ContextCompat.getColor(itemView.context, R.color.outline) // Excused (grey)
                else -> ContextCompat.getColor(itemView.context, R.color.outline)
            }
            statusIndicator.setBackgroundColor(statusColor)

            // Menu and status actions
            menuButton.setOnClickListener { onPlayerAction(player, "menu", it) }
            statusIndicator.setOnClickListener { onPlayerAction(player, "status_change", null) }
            playerName.setOnClickListener { onPlayerAction(player, "status_change", null) }
        }
    }

    private class PlayerDiffCallback(
        private val oldList: List<AttendanceWithUser>,
        private val newList: List<AttendanceWithUser>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].user.id == newList[newItemPosition].user.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

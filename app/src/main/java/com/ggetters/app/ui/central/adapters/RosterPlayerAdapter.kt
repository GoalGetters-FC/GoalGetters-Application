package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.ui.shared.extensions.getColorRes
import com.ggetters.app.ui.shared.extensions.getDisplayText
import com.ggetters.app.ui.shared.extensions.getIcon
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class RosterPlayerAdapter(
    private val onPlayerClick: (RosterPlayer) -> Unit,
    private val onRSVPChange: (RosterPlayer, RSVPStatus) -> Unit,
    private val onContactPlayer: (RosterPlayer) -> Unit
) : RecyclerView.Adapter<RosterPlayerAdapter.PlayerViewHolder>() {

    private var players = listOf<RosterPlayer>()

    fun updatePlayers(newPlayers: List<RosterPlayer>) {
        val diffCallback = PlayerDiffCallback(players, newPlayers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        players = newPlayers
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_availability, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerCard: MaterialCardView = itemView.findViewById(R.id.playerCard)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val playerPosition: TextView = itemView.findViewById(R.id.playerPosition)
        private val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        private val responseTime: TextView = itemView.findViewById(R.id.responseTime)
        private val availableButton: MaterialButton = itemView.findViewById(R.id.availableButton)
        private val maybeButton: MaterialButton = itemView.findViewById(R.id.maybeButton)
        private val unavailableButton: MaterialButton = itemView.findViewById(R.id.unavailableButton)
        private val contactButton: ImageButton = itemView.findViewById(R.id.contactButton)

        fun bind(player: RosterPlayer) {
            playerName.text = player.playerName
            playerPosition.text = player.position
            jerseyNumber.text = player.jerseyNumber.toString()

            updateStatusDisplay(player)
            setupQuickActions(player)
            updateCardStyling(player)

            responseTime.text = player.responseTime?.let { formatResponseTime(it) } ?: "No response"

            playerCard.setOnClickListener { onPlayerClick(player) }
            contactButton.setOnClickListener { onContactPlayer(player) }
        }

        private fun updateStatusDisplay(player: RosterPlayer) {
            statusChip.text = "${player.status.getIcon()} ${player.status.getDisplayText()}"
            statusChip.setChipBackgroundColorResource(player.status.getColorRes())
            statusChip.setTextColor(
                ContextCompat.getColor(itemView.context, player.status.getColorRes())
            )
        }

        private fun setupQuickActions(player: RosterPlayer) {
            availableButton.setOnClickListener {
                if (player.status != RSVPStatus.AVAILABLE) {
                    onRSVPChange(player, RSVPStatus.AVAILABLE)
                }
            }
            maybeButton.setOnClickListener {
                if (player.status != RSVPStatus.MAYBE) {
                    onRSVPChange(player, RSVPStatus.MAYBE)
                }
            }
            unavailableButton.setOnClickListener {
                if (player.status != RSVPStatus.UNAVAILABLE) {
                    onRSVPChange(player, RSVPStatus.UNAVAILABLE)
                }
            }
            updateButtonStates(player)
        }

        private fun updateButtonStates(player: RosterPlayer) {
            listOf(availableButton, maybeButton, unavailableButton).forEach { button ->
                button.strokeWidth = 1
                button.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.text_secondary)
                )
            }

            val selectedButton = when (player.status) {
                RSVPStatus.AVAILABLE -> availableButton
                RSVPStatus.MAYBE -> maybeButton
                RSVPStatus.UNAVAILABLE -> unavailableButton
                RSVPStatus.NOT_RESPONDED -> null
            }

            selectedButton?.let { button ->
                button.strokeWidth = 3
                button.setTextColor(
                    ContextCompat.getColor(itemView.context, player.status.getColorRes())
                )
                button.strokeColor = ContextCompat.getColorStateList(
                    itemView.context,
                    player.status.getColorRes()
                )
            }
        }

        private fun updateCardStyling(player: RosterPlayer) {
            val cardBackground = when (player.status) {
                RSVPStatus.AVAILABLE -> R.color.success_light
                RSVPStatus.MAYBE -> R.color.warning_light
                RSVPStatus.UNAVAILABLE -> R.color.error_light
                RSVPStatus.NOT_RESPONDED -> R.color.surface_container
            }
            playerCard.setCardBackgroundColor(ContextCompat.getColor(itemView.context, cardBackground))
            playerCard.strokeColor = ContextCompat.getColor(itemView.context, player.status.getColorRes())
            playerCard.strokeWidth = 2
        }

        private fun formatResponseTime(responseTime: Instant): String {
            val now = Instant.now()
            val hours = ChronoUnit.HOURS.between(responseTime, now)
            val days = ChronoUnit.DAYS.between(responseTime, now)
            return when {
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                else -> "Just now"
            }
        }
    }

    private class PlayerDiffCallback(
        private val oldList: List<RosterPlayer>,
        private val newList: List<RosterPlayer>
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

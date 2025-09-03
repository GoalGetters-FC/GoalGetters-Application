package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.PlayerMatchStats
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * RecyclerView adapter for displaying player match statistics.
 *
 * @param onPlayerClick Callback when a player stat card is clicked.
 */
class PlayerStatsAdapter(
    private val onPlayerClick: (PlayerMatchStats) -> Unit
) : RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatsViewHolder>() {

    private var playerStats: List<PlayerMatchStats> = emptyList()

    fun updateStats(newStats: List<PlayerMatchStats>) {
        val diffCallback = StatsDiffCallback(playerStats, newStats)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        playerStats = newStats.sortedByDescending { it.rating }
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerStatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_stats, parent, false)
        return PlayerStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerStatsViewHolder, position: Int) {
        holder.bind(playerStats[position])
    }

    override fun getItemCount(): Int = playerStats.size

    inner class PlayerStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerCard: MaterialCardView = itemView.findViewById(R.id.playerCard)
        private val playerName: TextView = itemView.findViewById(R.id.playerName)
        private val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
        private val playerPosition: TextView = itemView.findViewById(R.id.playerPosition)
        private val playerRating: TextView = itemView.findViewById(R.id.playerRating)
        private val ratingProgress: LinearProgressIndicator = itemView.findViewById(R.id.ratingProgress)
        private val minutesPlayed: TextView = itemView.findViewById(R.id.minutesPlayed)
        private val goals: TextView = itemView.findViewById(R.id.goals)
        private val assists: TextView = itemView.findViewById(R.id.assists)
        private val shots: TextView = itemView.findViewById(R.id.shots)
        private val passes: TextView = itemView.findViewById(R.id.passes)
        private val passAccuracy: TextView = itemView.findViewById(R.id.passAccuracy)
        private val tackles: TextView = itemView.findViewById(R.id.tackles)
        private val fouls: TextView = itemView.findViewById(R.id.fouls)
        private val cards: TextView = itemView.findViewById(R.id.cards)

        fun bind(stats: PlayerMatchStats) {
            playerName.text = stats.playerName
            jerseyNumber.text = stats.jerseyNumber.toString()
            playerPosition.text = stats.position
            playerRating.text = String.format("%.1f", stats.rating)

            // Rating bar (scale 0–100)
            ratingProgress.progress = (stats.rating * 10).toInt()

            // Rating color
            val ratingColor = when {
                stats.rating >= 8.0 -> ContextCompat.getColor(itemView.context, R.color.success)
                stats.rating >= 7.0 -> ContextCompat.getColor(itemView.context, R.color.warning)
                stats.rating >= 6.0 -> ContextCompat.getColor(itemView.context, R.color.info)
                else -> ContextCompat.getColor(itemView.context, R.color.error)
            }
            playerRating.setTextColor(ratingColor)
            ratingProgress.setIndicatorColor(ratingColor)

            // Match stats
            minutesPlayed.text = "${stats.minutesPlayed}'"
            goals.text = stats.goals.toString()
            assists.text = stats.assists.toString()
            shots.text = "${stats.shotsOnTarget}/${stats.shots}"
            passes.text = "${stats.passes} (${stats.passAccuracy}%)"
            passAccuracy.text = "${stats.passAccuracy}%"
            tackles.text = stats.tackles.toString()
            fouls.text = stats.fouls.toString()

            // Cards
            cards.text = when {
                stats.redCards > 0 -> "🟥 ${stats.redCards}"
                stats.yellowCards > 0 -> "🟡 ${stats.yellowCards}"
                else -> "-"
            }

            // Highlight performances
            when {
                stats.goals >= 2 -> highlightCard(R.color.success)
                stats.rating >= 8.5 -> highlightCard(R.color.primary)
                stats.redCards > 0 -> highlightCard(R.color.error)
                else -> playerCard.strokeWidth = 0
            }

            playerCard.setOnClickListener { onPlayerClick(stats) }
        }

        private fun highlightCard(colorRes: Int) {
            playerCard.strokeColor = ContextCompat.getColor(itemView.context, colorRes)
            playerCard.strokeWidth = 3
        }
    }

    private class StatsDiffCallback(
        private val oldList: List<PlayerMatchStats>,
        private val newList: List<PlayerMatchStats>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].playerId == newList[newItemPosition].playerId
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}

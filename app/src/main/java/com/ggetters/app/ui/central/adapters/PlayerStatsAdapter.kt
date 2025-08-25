package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.PlayerMatchStats
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

class PlayerStatsAdapter(
    private val onPlayerClick: (PlayerMatchStats) -> Unit
) : RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatsViewHolder>() {

    private var playerStats = listOf<PlayerMatchStats>()

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
            
            // Set rating progress (out of 10)
            ratingProgress.progress = (stats.rating * 10).toInt()
            
            // Update rating color based on performance
            val ratingColor = when {
                stats.rating >= 8.0 -> ContextCompat.getColor(itemView.context, R.color.success)
                stats.rating >= 7.0 -> ContextCompat.getColor(itemView.context, R.color.warning)
                stats.rating >= 6.0 -> ContextCompat.getColor(itemView.context, R.color.info)
                else -> ContextCompat.getColor(itemView.context, R.color.error)
            }
            playerRating.setTextColor(ratingColor)
            ratingProgress.setIndicatorColor(ratingColor)
            
            // Statistics
            minutesPlayed.text = "${stats.minutesPlayed}'"
            goals.text = stats.goals.toString()
            assists.text = stats.assists.toString()
            shots.text = "${stats.shotsOnTarget}/${stats.shots}"
            passes.text = "${stats.passes} (${stats.passAccuracy}%)"
            passAccuracy.text = "${stats.passAccuracy}%"
            tackles.text = stats.tackles.toString()
            fouls.text = stats.fouls.toString()
            
            // Cards display
            val cardText = when {
                stats.redCards > 0 -> "🟥 ${stats.redCards}"
                stats.yellowCards > 0 -> "🟡 ${stats.yellowCards}"
                else -> "-"
            }
            cards.text = cardText
            
            // Highlight outstanding performances
            when {
                stats.goals >= 2 -> {
                    playerCard.strokeColor = ContextCompat.getColor(itemView.context, R.color.success)
                    playerCard.strokeWidth = 3
                }
                stats.rating >= 8.5 -> {
                    playerCard.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                    playerCard.strokeWidth = 3
                }
                stats.redCards > 0 -> {
                    playerCard.strokeColor = ContextCompat.getColor(itemView.context, R.color.error)
                    playerCard.strokeWidth = 3
                }
                else -> {
                    playerCard.strokeWidth = 0
                }
            }
            
            // Click listener
            playerCard.setOnClickListener { onPlayerClick(stats) }
        }
    }

    private class StatsDiffCallback(
        private val oldList: List<PlayerMatchStats>,
        private val newList: List<PlayerMatchStats>
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


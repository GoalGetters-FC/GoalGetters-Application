package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.ui.shared.extensions.getEventDescription
import com.ggetters.app.ui.shared.extensions.getFormattedTime
import com.google.android.material.card.MaterialCardView

class MatchEventAdapter(
    private val onEventClick: (MatchEvent) -> Unit,
    private val onEventLongClick: (MatchEvent) -> Unit
) : RecyclerView.Adapter<MatchEventAdapter.EventViewHolder>() {

    private var events = listOf<MatchEvent>()

    fun updateEvents(newEvents: List<MatchEvent>) {
        val diffCallback = EventDiffCallback(events, newEvents)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        events = newEvents.sortedByDescending { it.timestamp } // Show newest first
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventCard: MaterialCardView = itemView.findViewById(R.id.eventCard)
        private val eventIcon: TextView = itemView.findViewById(R.id.eventIcon)
        private val eventTime: TextView = itemView.findViewById(R.id.eventTime)
        private val eventDescription: TextView = itemView.findViewById(R.id.eventDescription)
        private val eventDetails: TextView = itemView.findViewById(R.id.eventDetails)

        fun bind(event: MatchEvent) {
            eventTime.text = event.getFormattedTime()
            eventDescription.text = event.getEventDescription()
            
            // Set event icon and styling based on type
            updateEventStyling(event)
            
            // Set additional details if available
            eventDetails.text = when (event.eventType) {
                MatchEventType.GOAL -> {
                    val goalType = event.details["goalType"] as? String
                    goalType?.let { "($it)" } ?: ""
                }
                MatchEventType.SUBSTITUTION -> {
                    val playerOut = event.details["playerOut"] as? String
                    val playerIn = event.details["playerIn"] as? String
                    if (playerOut != null && playerIn != null) {
                        "Sub: $playerIn ↔ $playerOut"
                    } else ""
                }
                MatchEventType.YELLOW_CARD, MatchEventType.RED_CARD -> {
                    event.details["reason"] as? String ?: ""
                }
                else -> ""
            }
            
            eventDetails.visibility = if (eventDetails.text.isEmpty()) View.GONE else View.VISIBLE
            
            // Click listeners
            eventCard.setOnClickListener { onEventClick(event) }
            eventCard.setOnLongClickListener { 
                onEventLongClick(event)
                true
            }
        }

        private fun updateEventStyling(event: MatchEvent) {
            val iconText: String
            val backgroundColor: Int
            val iconColor: Int
            
            when (event.eventType) {
                MatchEventType.GOAL -> {
                    iconText = "⚽"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.success_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.success)
                }
                MatchEventType.YELLOW_CARD -> {
                    iconText = "🟡"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.warning_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.warning)
                }
                MatchEventType.RED_CARD -> {
                    iconText = "🟥"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.error_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.error)
                }
                MatchEventType.SUBSTITUTION -> {
                    iconText = "🔄"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.info_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.info)
                }
                MatchEventType.MATCH_START -> {
                    iconText = "▶️"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.success_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.success)
                }
                MatchEventType.MATCH_END -> {
                    iconText = "⏹️"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.surface_variant)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.on_surface)
                }
                MatchEventType.HALF_TIME -> {
                    iconText = "⏸️"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.warning_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.warning)
                }
                MatchEventType.SCORE_UPDATE -> {
                    iconText = "📊"
                    backgroundColor = ContextCompat.getColor(itemView.context, R.color.info_light)
                    iconColor = ContextCompat.getColor(itemView.context, R.color.info)
                }
            }
            
            eventIcon.text = iconText
            eventIcon.setTextColor(iconColor)
            eventCard.setCardBackgroundColor(backgroundColor)
            
            // Add stroke for important events
            when (event.eventType) {
                MatchEventType.GOAL, MatchEventType.RED_CARD -> {
                    eventCard.strokeColor = iconColor
                    eventCard.strokeWidth = 3
                }
                else -> {
                    eventCard.strokeWidth = 0
                }
            }
        }
    }

    private class EventDiffCallback(
        private val oldList: List<MatchEvent>,
        private val newList: List<MatchEvent>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}


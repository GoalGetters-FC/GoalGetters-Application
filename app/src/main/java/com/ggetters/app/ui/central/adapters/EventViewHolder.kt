package com.ggetters.app.ui.central.adapters

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType

class EventViewHolder(
    itemView: View,
    private val onClick: (Event) -> Unit,
    private val onLongClick: (Event) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val TAG = "EventViewHolder"
    }
    
    
    // --- Fields
    
    
    private val eventTypeIcon: TextView = itemView.findViewById(R.id.eventTypeIcon)
    private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
    private val eventTime: TextView = itemView.findViewById(R.id.eventTime)
    private val eventVenue: TextView = itemView.findViewById(R.id.eventVenue)
    private val eventOpponent: TextView = itemView.findViewById(R.id.eventOpponent)
    private val eventContainer: View = itemView.findViewById(R.id.eventContainer)

    
    // --- Functions

    
    /**
     * Binds the data to the view.
     */
    fun bind(
        event: Event
    ) {
        eventTitle.text = event.title
        eventTime.text = event.time
        eventVenue.text = event.venue

        // Set event type icon and color
        eventTypeIcon.text = event.type.icon
        eventTypeIcon.setTextColor(Color.parseColor(event.type.color))

        // Show opponent for games
        if (event.type == EventType.MATCH && !event.opponent.isNullOrBlank()) {
            eventOpponent.visibility = View.VISIBLE
            eventOpponent.text = "vs ${event.opponent}"
        } else {
            eventOpponent.visibility = View.GONE
        }

        // Set click listeners
        eventContainer.setOnClickListener {
            onClick(event)
        }

        eventContainer.setOnLongClickListener {
            onLongClick(event)
            true
        }
    }
}
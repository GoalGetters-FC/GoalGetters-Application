package com.ggetters.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.models.Event
import com.ggetters.app.ui.models.EventType

class EventAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onEventLongClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events = listOf<Event>()

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents.sortedBy { it.date }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTypeIcon: TextView = itemView.findViewById(R.id.eventTypeIcon)
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        private val eventTime: TextView = itemView.findViewById(R.id.eventTime)
        private val eventVenue: TextView = itemView.findViewById(R.id.eventVenue)
        private val eventOpponent: TextView = itemView.findViewById(R.id.eventOpponent)
        private val eventContainer: View = itemView.findViewById(R.id.eventContainer)

        fun bind(event: Event) {
            eventTitle.text = event.title
            eventTime.text = event.time
            eventVenue.text = event.venue
            
            // Set event type icon and color
            eventTypeIcon.text = event.type.icon
            eventTypeIcon.setTextColor(android.graphics.Color.parseColor(event.type.color))
            
            // Show opponent for games
            if (event.type == EventType.GAME && !event.opponent.isNullOrBlank()) {
                eventOpponent.visibility = View.VISIBLE
                eventOpponent.text = "vs ${event.opponent}"
            } else {
                eventOpponent.visibility = View.GONE
            }
            
            // Set click listeners
            eventContainer.setOnClickListener {
                onEventClick(event)
            }
            
            eventContainer.setOnLongClickListener {
                onEventLongClick(event)
                true
            }
        }
    }
} 
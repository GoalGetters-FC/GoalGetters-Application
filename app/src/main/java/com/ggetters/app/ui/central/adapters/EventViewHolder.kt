package com.ggetters.app.ui.central.adapters

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ItemEventBinding
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType

class EventViewHolder(
    private val binding: ItemEventBinding,
    private val onClick: (Event) -> Unit,
    private val onLongClick: (Event) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        private const val TAG = "EventViewHolder"
        private const val DEV_VERBOSE_LOGGER = true
    }

    
    // --- Functions

    
    /**
     * Binds the data to the view.
     */
    fun bind(
        item: Event
    ) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG, "<bind>: id=[${item.id}]"
        )


        binding.eventTitle.text = item.title
        binding.eventTime.text = item.time
        binding.eventVenue.text = item.venue

        // Set event type icon and color
        binding.eventTypeIcon.text = item.type.icon
        binding.eventTypeIcon.setTextColor(Color.parseColor(item.type.color))

        // Show opponent for games
        if (item.type == EventType.MATCH && !item.opponent.isNullOrBlank()) {
            binding.eventOpponent.visibility = View.VISIBLE
            binding.eventOpponent.text = "vs ${item.opponent}"
        } else {
            binding.eventOpponent.visibility = View.GONE
        }

        // Set click listeners
        binding.eventContainer.setOnClickListener {
            onClick(item)
        }

        binding.eventContainer.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }
}
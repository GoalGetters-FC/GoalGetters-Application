package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import com.ggetters.app.databinding.ItemEventBinding
import java.time.format.DateTimeFormatter

class EventViewHolder(
    private val binding: ItemEventBinding,
    private val onClick: (Event) -> Unit,
    private val onLongClick: (Event) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val TAG = "EventViewHolder"
        private const val DEV_VERBOSE_LOGGER = true
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    fun bind(item: Event) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "<bind>: id=[${item.id}]")

        // Title
        binding.eventTitle.text = item.title

        // Time & Venue
        binding.eventTime.text = item.time
        binding.eventVenue.text = item.venue

        // Category icon
        binding.eventTypeIcon.text = when (item.type) {
            EventType.MATCH -> "⚽"
            EventType.PRACTICE -> "🏋️"
            else -> "📌" // Default for OTHER or any new categories
        }

        // Opponent line → show opponent if available
        if (!item.opponent.isNullOrBlank()) {
            binding.eventOpponent.visibility = View.VISIBLE
            binding.eventOpponent.text = item.opponent
        } else {
            binding.eventOpponent.visibility = View.GONE
        }

        // Click listeners
        binding.eventContainer.setOnClickListener { onClick(item) }
        binding.eventContainer.setOnLongClickListener { onLongClick(item); true }
    }

}

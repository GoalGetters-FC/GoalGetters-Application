package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
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
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    
    // --- Functions

    fun bind(item: Event) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "<bind>: id=[${item.id}]")

        binding.eventTitle.text = item.name
        binding.eventTime.text = item.startAt.format(timeFormatter)
        binding.eventVenue.text = item.location ?: "TBD"

        // Example: simple icon/color decision based on category
        binding.eventTypeIcon.text = when (item.category) {
            EventCategory.MATCH -> "⚽"
            EventCategory.PRACTICE -> "🏋️"
            EventCategory.OTHER -> "📌"
        }

        // Example: show description if available
        if (!item.description.isNullOrBlank()) {
            binding.eventOpponent.visibility = View.VISIBLE
            binding.eventOpponent.text = item.description
        } else {
            binding.eventOpponent.visibility = View.GONE
        }

        binding.eventContainer.setOnClickListener { onClick(item) }
        binding.eventContainer.setOnLongClickListener { onLongClick(item); true }
    }
}
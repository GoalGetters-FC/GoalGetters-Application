package com.ggetters.app.ui.central.adapters

import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.databinding.ItemCalendarEventBinding
import java.time.format.DateTimeFormatter

class EventViewHolder(
    private val binding: ItemCalendarEventBinding,
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
        binding.eventTitle.text = item.name

        // Time & Venue
        binding.eventTime.text = item.startAt.format(timeFormatter)
        binding.eventVenue.text = item.location ?: "No venue"

        // TODO: Conditional event icons

        // Click listeners
        binding.eventContainer.setOnClickListener { onClick(item) }
        binding.eventContainer.setOnLongClickListener { onLongClick(item); true }
    }

}

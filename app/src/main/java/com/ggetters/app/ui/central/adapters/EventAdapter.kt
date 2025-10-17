package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.databinding.ItemCalendarEventBinding
import com.ggetters.app.ui.shared.adapters.KeyedDiffCallback

class EventAdapter(
    private val onClick: (Event) -> Unit,
    private val onLongClick: (Event) -> Unit
) : ListAdapter<Event, EventViewHolder>(KeyedDiffCallback<Event>()) {

    companion object {
        private const val TAG = "EventAdapter"
        private const val DEV_VERBOSE_LOGGER = true
    }


    // --- Functions (Contract)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "Constructing the ViewHolder")
        return EventViewHolder(
            binding = ItemCalendarEventBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClick = onClick,
            onLongClick = onLongClick
        )
    }


    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "<onBindViewHolder>: position=[$position]")
        holder.bind(getItem(position))
    }

    // --- Functions (Helpers)


    fun update(collection: List<Event>) {
        Clogger.d(TAG, "Updating the source collection")
        val sortedCollection = collection.sortedBy { it.startAt }
        submitList(sortedCollection)
    }
} 
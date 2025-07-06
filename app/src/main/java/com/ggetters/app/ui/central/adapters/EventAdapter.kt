package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.Event

class EventAdapter(
    private val onClick: (Event) -> Unit, 
    private val onLongClick: (Event) -> Unit
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    companion object {
        private const val TAG = "EventAdapter"
        private const val DEV_VERBOSE_LOGGER = true
    }


    // --- Functions (Contract)


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EventViewHolder {
        Clogger.d(
            TAG, "Constructing the ViewHolder"
        )

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view, onClick, onLongClick)
    }


    override fun onBindViewHolder(
        holder: EventViewHolder, position: Int
    ) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG, "<onBindViewHolder>: position=[$position]"
        )

        holder.bind(getItem(position))
    }


    // --- Functions (Helpers)


    fun update(collection: List<Event>) {
        Clogger.d(
            TAG, "Updating the source collection"
        )

        submitList(collection.sortedBy {
            it.date
        })
    }
} 
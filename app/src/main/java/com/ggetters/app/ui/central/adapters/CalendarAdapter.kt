package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ItemCalendarDayBinding
import com.ggetters.app.ui.central.models.CalendarDayItem

class CalendarAdapter(
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : ListAdapter<CalendarDayItem, CalendarViewHolder>(CalendarDiffCallback()) {
    companion object {
        private const val TAG = "CalendarAdapter"
        private const val DEV_VERBOSE_LOGGER = true
    }


    // --- Functions (Contract)


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CalendarViewHolder {
        Clogger.d(
            TAG, "Constructing the ViewHolder"
        )

        // Construct the binding and return the view holder
        return CalendarViewHolder(
            binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onClick, onLongClick
        )
    }


    override fun onBindViewHolder(
        holder: CalendarViewHolder, position: Int
    ) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG, "<onBindViewHolder>: position=[$position]"
        )

        holder.bind(getItem(position))
    }


    // --- Functions (Helpers)


    fun update(collection: List<CalendarDayItem>) {
        Clogger.d(
            TAG, "Updating the source collection"
        )

        submitList(collection)
    }
} 
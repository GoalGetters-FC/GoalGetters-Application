package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ItemCalendarDayBinding
import com.ggetters.app.ui.central.models.CalendarDayItem

class CalendarAdapter(
    private val onDayClick: (Int) -> Unit, private val onDayLongClick: (Int) -> Unit
) : ListAdapter<CalendarDayItem, CalendarViewHolder>(CalendarDiffCallback()) {
    companion object {
        private const val TAG = "CalendarAdapter"
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
            ), onDayClick, onDayLongClick
        )
    }


    override fun onBindViewHolder(
        holder: CalendarViewHolder, position: Int
    ) {
        Clogger.d(
            TAG, "<onBindViewHolder>: position=[$position]"
        )

        holder.bind(getItem(position))
    }


    // --- Functions (Helpers)


    fun updateCollection(updatedCollection: List<CalendarDayItem>) {
        submitList(updatedCollection)
    }
} 
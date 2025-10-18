package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.CalendarDayItem
import com.ggetters.app.databinding.ItemCalendarDayBinding
import com.ggetters.app.ui.shared.adapters.KeyedDiffCallback

class CalendarAdapter(
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : ListAdapter<CalendarDayItem, CalendarViewHolder>(KeyedDiffCallback<CalendarDayItem>()) {

    companion object {
        private const val TAG = "CalendarAdapter"
        private const val DEV_VERBOSE_LOGGER = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "Constructing the ViewHolder")
        return CalendarViewHolder(
            binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onClick = onClick,
            onLongClick = onLongClick
        )
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        if (DEV_VERBOSE_LOGGER) {
            Clogger.d(TAG, "<onBindViewHolder>: position=[$position]")
        }
        holder.bind(getItem(position))
    }

    fun update(collection: List<CalendarDayItem>) {
        submitList(collection)
    }
}

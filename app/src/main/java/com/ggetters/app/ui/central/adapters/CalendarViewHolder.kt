package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.CalendarDayItem   // ✅ from ui.central.models
import com.ggetters.app.ui.central.models.EventType     // ✅ use EventType instead of EventCategory
import com.ggetters.app.databinding.ItemCalendarDayBinding

class CalendarViewHolder(
    private val binding: ItemCalendarDayBinding,
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val TAG = "CalendarViewHolder"
        private const val DEV_VERBOSE_LOGGER = true
    }

    fun bind(item: CalendarDayItem) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "<bind>: id=[${item.id}]")
        setupNumberedDay(item)
    }

    private fun setupNumberedDay(calendarDay: CalendarDayItem) {
        if (calendarDay.dayNumber == null) {
            binding.dayNumber.text = ""
            binding.dayContainer.isClickable = false
            binding.dayContainer.isFocusable = false
            binding.eventDot1.visibility = View.GONE
            binding.eventDot2.visibility = View.GONE
            binding.eventDot3.visibility = View.GONE
            binding.dayContainer.alpha = 0.3f // Dim
        } else {
            binding.dayNumber.text = calendarDay.dayNumber.toString()
            binding.dayContainer.isClickable = true
            binding.dayContainer.isFocusable = true

            binding.dayContainer.setOnClickListener {
                if (calendarDay.isCurrentMonth) onClick(calendarDay.dayNumber)
            }

            binding.dayContainer.setOnLongClickListener {
                if (calendarDay.isCurrentMonth) {
                    onLongClick(calendarDay.dayNumber)
                    true
                } else false
            }

            renderEventBackground(calendarDay)
            renderEventDots(calendarDay)

            if (calendarDay.isCurrentMonth) {
                binding.dayContainer.background = AppCompatResources.getDrawable(
                    itemView.context, R.drawable.day_ripple_background
                )
            }
        }
    }

    private fun renderEventBackground(calendarDay: CalendarDayItem) {
        when {
            calendarDay.isSelected -> {
                binding.dayContainer.setBackgroundResource(R.drawable.selected_day_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                binding.dayContainer.elevation = 4f
            }
            calendarDay.isToday -> {
                binding.dayContainer.setBackgroundResource(R.drawable.today_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                binding.dayContainer.elevation = 2f
            }
            calendarDay.isCurrentMonth -> {
                binding.dayContainer.setBackgroundResource(R.drawable.day_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                binding.dayContainer.elevation = 0f
            }
            else -> {
                binding.dayContainer.setBackgroundResource(R.drawable.other_month_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.text_disabled))
                binding.dayContainer.elevation = 0f
            }
        }
    }

    private fun renderEventDots(calendarDay: CalendarDayItem) {
        val eventTypes = calendarDay.events.map { it.type }.toSet()

        updateEventDotVisibility(binding.eventDot1, EventType.PRACTICE, eventTypes)
        updateEventDotVisibility(binding.eventDot2, EventType.MATCH, eventTypes)
        updateEventDotVisibility(binding.eventDot3, EventType.OTHER, eventTypes)
    }

    private fun updateEventDotVisibility(
        eventDot: View,
        eventType: EventType,
        eventTypes: Set<EventType>
    ) {
        eventDot.visibility = if (eventType in eventTypes) View.VISIBLE else View.GONE
    }
}

package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.CalendarDayItem   // ✅ from data.model
import com.ggetters.app.data.model.EventCategory     // ✅ use EventCategory instead of EventType
import com.ggetters.app.databinding.ItemCalendarDayBinding

class CalendarViewHolder(
    private val binding: ItemCalendarDayBinding,
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val TAG = "CalendarViewHolder"
        private const val DEV_VERBOSE_LOGGER = false
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
                    itemView.context, R.drawable.calendar_day_ripple_background
                )
            }
        }
    }

    private fun renderEventBackground(calendarDay: CalendarDayItem) {
        when {
            calendarDay.isSelected -> {
                binding.dayContainer.setBackgroundResource(R.drawable.calendar_day_selected_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                binding.dayContainer.elevation = 4f
            }
            calendarDay.isToday -> {
                binding.dayContainer.setBackgroundResource(R.drawable.calendar_day_today_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                binding.dayContainer.elevation = 2f
            }
            calendarDay.isCurrentMonth -> {
                binding.dayContainer.setBackgroundResource(R.drawable.calendar_day_background)
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
        val eventCategories = calendarDay.events.map { it.category }.toSet()

        updateEventDotVisibility(binding.eventDot1, EventCategory.PRACTICE, eventCategories)
        updateEventDotVisibility(binding.eventDot2, EventCategory.MATCH, eventCategories)
        updateEventDotVisibility(binding.eventDot3, EventCategory.OTHER, eventCategories)
    }

    private fun updateEventDotVisibility(
        eventDot: View,
        eventCategory: EventCategory,
        eventCategories: Set<EventCategory>
    ) {
        eventDot.visibility = if (eventCategory in eventCategories) View.VISIBLE else View.GONE
    }
}

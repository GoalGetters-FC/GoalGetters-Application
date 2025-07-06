package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ItemCalendarDayBinding
import com.ggetters.app.ui.central.models.CalendarDayItem
import com.ggetters.app.ui.central.models.EventType

class CalendarViewHolder(
    private val binding: ItemCalendarDayBinding,
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        private const val TAG = "CalendarViewHolder"
    }


    // --- Functions


    /**
     * Binds the data to the view.
     */
    fun bind(
        calendarDay: CalendarDayItem
    ) {
        Clogger.d(
            TAG, "<bind>: id=[${calendarDay.id}]"
        )

        setupNumberedDay(calendarDay)
    }


    /**
     * Sets up the day number based on the calendar day item.
     */
    private fun setupNumberedDay(
        calendarDay: CalendarDayItem
    ) = when (calendarDay.dayNumber == null) {
        true -> {
            binding.dayNumber.text = ""
            binding.dayNumber.isClickable = false
            binding.dayContainer.isClickable = false
            binding.dayContainer.isFocusable = false
            binding.eventDot1.visibility = View.GONE
            binding.eventDot2.visibility = View.GONE
            binding.eventDot3.visibility = View.GONE
            binding.dayContainer.alpha = 0.3f // Dim
        }

        else -> {
            binding.dayNumber.text = calendarDay.dayNumber.toString()
            binding.dayContainer.isClickable = true
            binding.dayContainer.isFocusable = true

            binding.dayContainer.setOnClickListener {
                if (calendarDay.isCurrentMonth) {
                    onClick(calendarDay.dayNumber)
                }
            }

            binding.dayContainer.setOnLongClickListener {
                if (calendarDay.isCurrentMonth) {
                    onLongClick(calendarDay.dayNumber)
                    true
                } else {
                    false
                }
            }

            renderEventBackground(calendarDay)
            renderEventDots(calendarDay)

            // Add ripple effect for current month days
            if (calendarDay.isCurrentMonth) {
                binding.dayContainer.background = AppCompatResources.getDrawable(
                    itemView.context, R.drawable.day_ripple_background
                )
            } else {
            }
        }
    }


    /**
     * Draws the background resource based on the calendar day item.
     */
    private fun renderEventBackground(
        calendarDay: CalendarDayItem
    ) {
        if (calendarDay.isToday) {
            binding.dayContainer.setBackgroundResource(R.drawable.today_background)
            binding.dayNumber.setTextColor(itemView.context.getColor(R.color.white))
            return
        }

        when (calendarDay.isCurrentMonth) {
            true -> {
                binding.dayContainer.setBackgroundResource(R.drawable.day_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.black))
            }

            else -> {
                binding.dayContainer.setBackgroundResource(R.drawable.other_month_background)
                binding.dayNumber.setTextColor(itemView.context.getColor(R.color.text_disabled))
            }
        }
    }


    /**
     * Draws the event dots based on the event types.
     */
    private fun renderEventDots(
        calendarDay: CalendarDayItem
    ) {
        val eventTypes = calendarDay.events.map {
            it.type
        }.toSet()

        updateEventDotVisibility(binding.eventDot1, EventType.PRACTICE, eventTypes)
        updateEventDotVisibility(binding.eventDot2, EventType.MATCH, eventTypes)
        updateEventDotVisibility(binding.eventDot3, EventType.OTHER, eventTypes)
    }


    /**
     * Updates the visibility of the event dot based on the event type.
     */
    private fun updateEventDotVisibility(
        eventDot: View, eventType: EventType, eventTypes: Set<EventType>
    ) {
        eventDot.visibility = if (eventType in eventTypes) {
            View.VISIBLE
        } else View.GONE
    }
}
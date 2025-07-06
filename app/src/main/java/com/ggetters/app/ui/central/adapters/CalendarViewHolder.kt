package com.ggetters.app.ui.central.adapters

import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.CalendarDayItem
import com.ggetters.app.ui.central.models.EventType

class CalendarViewHolder(
    itemView: View,
    private val onDayClick: (Int) -> Unit,
    private val onDayLongClick: (Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val TAG = "CalendarViewHolder"
    }


    // --- Fields


    private val dayNumber: TextView = itemView.findViewById(R.id.dayNumber)
    private val dayContainer: View = itemView.findViewById(R.id.dayContainer)
    private val eventDot1: View = itemView.findViewById(R.id.eventDot1)
    private val eventDot2: View = itemView.findViewById(R.id.eventDot2)
    private val eventDot3: View = itemView.findViewById(R.id.eventDot3)


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
            dayNumber.text = ""
            dayNumber.isClickable = false
            dayContainer.isClickable = false
            dayContainer.isFocusable = false
            eventDot1.visibility = View.GONE
            eventDot2.visibility = View.GONE
            eventDot3.visibility = View.GONE
            dayContainer.alpha = 0.3f // Dim
        }

        else -> {
            dayNumber.text = calendarDay.dayNumber.toString()
            dayContainer.isClickable = true
            dayContainer.isFocusable = true
            
            dayContainer.setOnClickListener {
                if (calendarDay.isCurrentMonth) {
                    onDayClick(calendarDay.dayNumber)
                }
            }

            dayContainer.setOnLongClickListener {
                if (calendarDay.isCurrentMonth) {
                    onDayLongClick(calendarDay.dayNumber)
                    true
                } else {
                    false
                }
            }

            renderEventBackground(calendarDay)
            renderEventDots(calendarDay)

            // Add ripple effect for current month days
            if (calendarDay.isCurrentMonth) {
                dayContainer.background = AppCompatResources.getDrawable(
                    itemView.context, R.drawable.day_ripple_background
                )
            } else {
                // No ripple effect for non-current month days
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
            dayContainer.setBackgroundResource(R.drawable.today_background)
            dayNumber.setTextColor(itemView.context.getColor(R.color.white))
            return
        } 
        
        when (calendarDay.isCurrentMonth) {
            true -> {
                dayContainer.setBackgroundResource(R.drawable.day_background)
                dayNumber.setTextColor(itemView.context.getColor(R.color.black))
            }
            
            else -> {
                dayContainer.setBackgroundResource(R.drawable.other_month_background)
                dayNumber.setTextColor(itemView.context.getColor(R.color.text_disabled))
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

        updateEventDotVisibility(eventDot1, EventType.PRACTICE, eventTypes)
        updateEventDotVisibility(eventDot2, EventType.MATCH, eventTypes)
        updateEventDotVisibility(eventDot3, EventType.OTHER, eventTypes)
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
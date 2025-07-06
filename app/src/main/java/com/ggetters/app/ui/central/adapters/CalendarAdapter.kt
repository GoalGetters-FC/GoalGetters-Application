package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.views.CalendarDayItem
import com.ggetters.app.ui.central.models.EventType

class CalendarAdapter(
    private val onDayClick: (Int) -> Unit,
    private val onDayLongClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var calendarDays = listOf<CalendarDayItem>()

    fun updateDays(days: List<CalendarDayItem>) {
        calendarDays = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(calendarDays[position])
    }

    override fun getItemCount(): Int = calendarDays.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayNumber: TextView = itemView.findViewById(R.id.dayNumber)
        private val eventDot1: View = itemView.findViewById(R.id.eventDot1)
        private val eventDot2: View = itemView.findViewById(R.id.eventDot2)
        private val eventDot3: View = itemView.findViewById(R.id.eventDot3)
        private val dayContainer: View = itemView.findViewById(R.id.dayContainer)

        fun bind(calendarDay: CalendarDayItem) {
            if (calendarDay.dayNumber == null) {
                // Empty day
                dayNumber.text = ""
                dayNumber.isClickable = false
                dayContainer.isClickable = false
                dayContainer.isFocusable = false
                eventDot1.visibility = View.GONE
                eventDot2.visibility = View.GONE
                eventDot3.visibility = View.GONE
                
                // Dim the empty day
                dayContainer.alpha = 0.3f
            } else {
                // Day with number
                dayNumber.text = calendarDay.dayNumber.toString()
                dayContainer.isClickable = true
                dayContainer.isFocusable = true
                
                // Set visual states
                if (calendarDay.isToday) {
                    dayContainer.setBackgroundResource(R.drawable.today_background)
                    dayNumber.setTextColor(itemView.context.getColor(R.color.white))
                } else if (calendarDay.isCurrentMonth) {
                    dayContainer.setBackgroundResource(R.drawable.day_background)
                    dayNumber.setTextColor(itemView.context.getColor(R.color.black))
                } else {
                    dayContainer.setBackgroundResource(R.drawable.other_month_background)
                    dayNumber.setTextColor(itemView.context.getColor(R.color.text_disabled))
                }
                
                // Set click listeners
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
                
                // Show event dots based on event types
                val practiceEvents = calendarDay.events.filter { it.type == EventType.PRACTICE }
                val gameEvents = calendarDay.events.filter { it.type == EventType.GAME }
                val otherEvents = calendarDay.events.filter { it.type == EventType.OTHER }
                
                eventDot1.visibility = if (practiceEvents.isNotEmpty()) View.VISIBLE else View.GONE
                eventDot2.visibility = if (gameEvents.isNotEmpty()) View.VISIBLE else View.GONE
                eventDot3.visibility = if (otherEvents.isNotEmpty()) View.VISIBLE else View.GONE
                
                // Add ripple effect for current month days
                if (calendarDay.isCurrentMonth) {
                    dayContainer.background = itemView.context.getDrawable(R.drawable.day_ripple_background)
                }
            }
        }
    }
} 
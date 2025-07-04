package com.ggetters.app.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ggetters.app.R
import com.ggetters.app.ui.adapters.CalendarAdapter
import com.ggetters.app.ui.models.Event
import com.ggetters.app.ui.models.EventType
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDayItem(
    val dayNumber: Int? = null,
    val events: List<Event> = emptyList()
)

class CalendarFragment : Fragment() {
    
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private var currentDate = Calendar.getInstance()
    private val events = mutableListOf<Event>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupCalendar()
        loadSampleEvents()
        updateCalendar()
    }
    
    private fun setupViews(view: View) {
        monthYearText = view.findViewById(R.id.monthYearText)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        
        val previousButton = view.findViewById<ImageButton>(R.id.previousMonthButton)
        val nextButton = view.findViewById<ImageButton>(R.id.nextMonthButton)
        val addEventFab = view.findViewById<FloatingActionButton>(R.id.addEventFab)
        
        previousButton.setOnClickListener {
            currentDate.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        nextButton.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        addEventFab.setOnClickListener {
            showAddEventBottomSheet()
        }
    }
    
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { day ->
            // Handle day click - show events for that day
            showEventsForDay(day)
        }
        
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }
    
    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = dateFormat.format(currentDate.time)
        
        val calendarDays = generateCalendarDays()
        calendarAdapter.updateDays(calendarDays)
    }
    
    private fun generateCalendarDays(): List<CalendarDayItem> {
        val days = mutableListOf<CalendarDayItem>()
        val calendar = currentDate.clone() as Calendar
        
        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Add empty days for previous month
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - 1
        
        for (i in 0 until startOffset) {
            days.add(CalendarDayItem())
        }
        
        // Add days of current month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dayCalendar = currentDate.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            
            val dayEvents = getEventsForDate(dayCalendar.time)
            days.add(CalendarDayItem(day, dayEvents))
        }
        
        return days
    }
    
    private fun getEventsForDate(date: Date): List<Event> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)
        
        return events.filter { event ->
            val eventCalendar = Calendar.getInstance()
            eventCalendar.time = event.date
            eventCalendar.get(Calendar.DAY_OF_MONTH) == targetDay &&
            eventCalendar.get(Calendar.MONTH) == targetMonth &&
            eventCalendar.get(Calendar.YEAR) == targetYear
        }
    }
    
    private fun loadSampleEvents() {
        val calendar = Calendar.getInstance()
        
        // Sample practice event
        calendar.set(2024, 11, 15) // December 15, 2024
        events.add(Event(
            id = "1",
            title = "Team Practice",
            type = EventType.PRACTICE,
            date = calendar.time,
            time = "15:00",
            venue = "Main Field",
            createdBy = "Coach"
        ))
        
        // Sample game event
        calendar.set(2024, 11, 22) // December 22, 2024
        events.add(Event(
            id = "2",
            title = "Match vs Eagles",
            type = EventType.GAME,
            date = calendar.time,
            time = "14:00",
            venue = "Stadium",
            opponent = "Eagles FC",
            createdBy = "Coach"
        ))
        
        // Sample general event
        calendar.set(2024, 11, 10) // December 10, 2024
        events.add(Event(
            id = "3",
            title = "Team Meeting",
            type = EventType.OTHER,
            date = calendar.time,
            time = "18:00",
            venue = "Club House",
            createdBy = "Manager"
        ))
    }
    
    private fun showEventsForDay(day: Int) {
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dayEvents = getEventsForDate(calendar.time)
        
        if (dayEvents.isNotEmpty()) {
            // TODO: Show events dialog/bottom sheet
        } else {
            // Show add event option
            showAddEventBottomSheet(day)
        }
    }
    
    private fun showAddEventBottomSheet(selectedDay: Int? = null) {
        val bottomSheet = AddEventBottomSheet()
        if (selectedDay != null) {
            val calendar = currentDate.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            bottomSheet.setSelectedDate(calendar.time)
        }
        bottomSheet.show(childFragmentManager, "AddEventBottomSheet")
    }
} 
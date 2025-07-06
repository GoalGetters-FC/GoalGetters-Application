package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.CalendarAdapter
import com.ggetters.app.ui.central.models.CalendarDayItem
import com.ggetters.app.ui.central.sheets.AddEventBottomSheet
import com.ggetters.app.ui.central.sheets.EventListBottomSheet
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var addEventFab: FloatingActionButton
    
    private var currentDate = Calendar.getInstance()
    private val events = mutableListOf<Event>()
    private var selectedDay: Int? = null
    
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
        setupSwipeGestures()
    }
    
    private fun setupViews(view: View) {
        monthYearText = view.findViewById(R.id.monthYearText)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        previousButton = view.findViewById(R.id.previousMonthButton)
        nextButton = view.findViewById(R.id.nextMonthButton)
        addEventFab = view.findViewById(R.id.addEventFab)
        
        previousButton.setOnClickListener {
            navigateToPreviousMonth()
        }
        
        nextButton.setOnClickListener {
            navigateToNextMonth()
        }
        
        addEventFab.setOnClickListener {
            showAddEventBottomSheet()
        }
    }
    
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter(
            onDayClick = { day ->
                handleDayClick(day)
            },
            onDayLongClick = { day ->
                handleDayLongClick(day)
            }
        )
        
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }
    
    private fun setupSwipeGestures() {
        // Add swipe gesture detection for month navigation
        calendarRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var startX = 0f
            private var startY = 0f
            private val SWIPE_THRESHOLD = 100f
            
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x
                        startY = e.y
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = e.x - startX
                        val deltaY = e.y - startY
                        
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0) {
                                navigateToPreviousMonth()
                            } else {
                                navigateToNextMonth()
                            }
                            return true
                        }
                    }
                }
                return false
            }
            
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }
    
    private fun navigateToPreviousMonth() {
        currentDate.add(Calendar.MONTH, -1)
        updateCalendar()
        animateMonthTransition(false)
    }
    
    private fun navigateToNextMonth() {
        currentDate.add(Calendar.MONTH, 1)
        updateCalendar()
        animateMonthTransition(true)
    }
    
    private fun animateMonthTransition(forward: Boolean) {
        // Simple fade animation for month transition
        calendarRecyclerView.alpha = 0.5f
        calendarRecyclerView.animate()
            .alpha(1.0f)
            .setDuration(300)
            .start()
    }
    
    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = dateFormat.format(currentDate.time)
        
        val calendarDays = generateCalendarDays()
        calendarAdapter.updateCollection(calendarDays)
    }
    
    private fun generateCalendarDays(): List<CalendarDayItem> {
        val days = mutableListOf<CalendarDayItem>()
        val calendar = currentDate.clone() as Calendar
        val today = Calendar.getInstance()
        
        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Add empty days for previous month
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - 1
        
        for (i in 0 until startOffset) {
            days.add(CalendarDayItem(isCurrentMonth = false))
        }
        
        // Add days of current month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dayCalendar = currentDate.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            
            val dayEvents = getEventsForDate(dayCalendar.time)
            val isToday = isSameDay(dayCalendar, today)
            
            days.add(CalendarDayItem(
                dayNumber = day,
                events = dayEvents,
                isCurrentMonth = true,
                isToday = isToday
            ))
        }
        
        return days
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
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
            title = "MATCH vs Eagles",
            type = EventType.MATCH,
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
        
        // Add some events for current month
        val currentCalendar = Calendar.getInstance()
        currentCalendar.add(Calendar.DAY_OF_MONTH, 2) // 2 days from now
        events.add(Event(
            id = "4",
            title = "Training Session",
            type = EventType.PRACTICE,
            date = currentCalendar.time,
            time = "16:00",
            venue = "Training Ground",
            createdBy = "Coach"
        ))
        
        currentCalendar.add(Calendar.DAY_OF_MONTH, 5) // 7 days from now
        events.add(Event(
            id = "5",
            title = "Friendly MATCH",
            type = EventType.MATCH,
            date = currentCalendar.time,
            time = "15:30",
            venue = "Local Stadium",
            opponent = "City Rovers",
            createdBy = "Coach"
        ))
    }
    
    private fun handleDayClick(day: Int) {
        selectedDay = day
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dayEvents = getEventsForDate(calendar.time)
        
        if (dayEvents.isNotEmpty()) {
            showEventsForDay(day, dayEvents)
        } else {
            showAddEventBottomSheet(day)
        }
    }
    
    private fun handleDayLongClick(day: Int) {
        // Long press to quickly add event
        showAddEventBottomSheet(day)
    }
    
    private fun showEventsForDay(day: Int, events: List<Event>) {
        val eventListBottomSheet = EventListBottomSheet.Companion.newInstance(day, events)
        eventListBottomSheet.show(childFragmentManager, "EventListBottomSheet")
    }
    
    private fun showAddEventBottomSheet(selectedDay: Int? = null) {
        val bottomSheet = AddEventBottomSheet()
        if (selectedDay != null) {
            val calendar = currentDate.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            bottomSheet.setSelectedDate(calendar.time)
        }
        bottomSheet.setOnEventCreatedListener { event ->
            events.add(event)
            updateCalendar()
        }
        bottomSheet.show(childFragmentManager, "AddEventBottomSheet")
    }
    
    fun addEvent(event: Event) {
        events.add(event)
        updateCalendar()
    }
    
    fun removeEvent(eventId: String) {
        events.removeAll { it.id == eventId }
        updateCalendar()
    }
} 
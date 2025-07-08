package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.FragmentCalendarBinding
import com.ggetters.app.ui.central.adapters.CalendarAdapter
import com.ggetters.app.ui.central.models.CalendarDayItem
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import com.ggetters.app.ui.central.sheets.AddEventBottomSheet
import com.ggetters.app.ui.central.sheets.EventListBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class CalendarFragment : Fragment(), Clickable {

    private lateinit var binds: FragmentCalendarBinding
    private lateinit var adapter: CalendarAdapter

    private var currentDate = Calendar.getInstance()
    private val events = mutableListOf<Event>()
    private var selectedDay: Int? = null


    // --- Lifecycle


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = createBindings(inflater, container)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCalendar()
        setupTouchListeners()

        adapter = binds.rvCalendar.adapter as CalendarAdapter
        
        seed()
    }


    // --- Internals


    /**
     * Increments or decrements the calendar preview by a number of months.
     *
     * - (+): Increment
     * - (-): Decrement
     *
     * @param months the number of months to increment or decrement.
     *
     * @throws UnsupportedOperationException when the value of [months] is
     *         zero, as this method should not be called in such cases and
     *         should be guarded against.
     */
    private fun offsetCalendarView(months: Int) {
        require(months != 0)
        currentDate.add(
            Calendar.MONTH, months
        )

        updateCalendarView()
        animateCalendarTransition()
    }


    /**
     * Convenience method for [offsetCalendarView].
     */
    private fun incrementCalendarView() = offsetCalendarView(months = -1)


    /**
     * Convenience method for [offsetCalendarView].
     */
    private fun decrementCalendarView() = offsetCalendarView(months = +1)


    /**
     * Animate the calendar view transition when switching months.
     */
    private fun animateCalendarTransition() {
        binds.rvCalendar.alpha = 0.5f
        binds.rvCalendar.animate().apply {
            alpha(1.0f)
            duration = 150
        }.start()
    }


    private fun updateCalendarView() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binds.monthYearText.text = dateFormat.format(currentDate.time)
        val calendarDays = generateCalendarDays()
        adapter.update(calendarDays)
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

            days.add(
                CalendarDayItem(
                    dayNumber = day, events = dayEvents, isCurrentMonth = true, isToday = isToday
                )
            )
        }

        return days
    }


    private fun isSameDay(
        oldCalendar: Calendar, newCalendar: Calendar
    ): Boolean {
        val isSameDay =
            oldCalendar.get(Calendar.DAY_OF_MONTH) == newCalendar.get(Calendar.DAY_OF_MONTH)
        val isSameMonth = oldCalendar.get(Calendar.MONTH) == newCalendar.get(Calendar.MONTH)
        val isSameYear = oldCalendar.get(Calendar.YEAR) == newCalendar.get(Calendar.YEAR)
        return (isSameDay && isSameMonth && isSameYear)
    }


    // TODO: Business logic should be in ViewModel
    private fun getEventsForDate(date: Date): List<Event> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        return events.filter { event ->
            val eventCalendar = Calendar.getInstance()
            eventCalendar.time = event.date
            eventCalendar.get(Calendar.DAY_OF_MONTH) == targetDay && eventCalendar.get(Calendar.MONTH) == targetMonth && eventCalendar.get(
                Calendar.YEAR
            ) == targetYear
        }
    }


    private fun onDayClickedCallback(dayOfMonth: Int) {
        selectedDay = dayOfMonth
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val dayEvents = getEventsForDate(calendar.time)

        if (dayEvents.isNotEmpty()) {
            showEventsForDay(dayOfMonth, dayEvents)
        } else {
            showAddEventBottomSheet(dayOfMonth)
        }
    }


    private fun onDayLongClickedCallback(dayOfMonth: Int) {
        showAddEventBottomSheet(dayOfMonth)
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
            updateCalendarView()
        }
        bottomSheet.show(childFragmentManager, "AddEventBottomSheet")
    }


    // --- Event Handlers


    override fun setupTouchListeners() {
        binds.btIncrementCalendar.setOnClickListener(this)
        binds.btDecrementCalendar.setOnClickListener(this)
        binds.fab.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btIncrementCalendar.id -> decrementCalendarView()
        binds.btDecrementCalendar.id -> incrementCalendarView()
        binds.fab.id -> showAddEventBottomSheet()
        else -> {
            Clogger.w(
                "TAG", "Unhandled on-click for: ${view?.id}"
            )
        }
    }


    // --- UI Configuration


    private fun setupCalendar() {
        binds.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = CalendarAdapter(onClick = { day ->
                onDayClickedCallback(day)
            }, onLongClick = { day ->
                onDayLongClickedCallback(day)
            })
        }

        setupSwipeGestures()
    }


    private fun setupSwipeGestures() {
        // Add swipe gesture detection for month navigation
        binds.rvCalendar.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {
            private var startX = 0f
            private var startY = 0f
            private val SWIPE_THRESHOLD = 100f

            override fun onInterceptTouchEvent(
                rv: RecyclerView, e: MotionEvent
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x
                        startY = e.y
                    }

                    MotionEvent.ACTION_UP -> {
                        val deltaX = e.x - startX
                        val deltaY = e.y - startY

                        if (abs(deltaX) > abs(deltaY) && abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0) {
                                incrementCalendarView()
                            } else {
                                decrementCalendarView()
                            }
                            return true
                        }
                    }
                }
                return false
            }

            // Contracted overrides that were not needed

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(
                disallowIntercept: Boolean
            ) {
            }
        })
    }


    // --- UI Registrations


    /**
     * Construct the view binding for this fragment.
     * 
     * @return the root [View] of this fragment within the same context as every
     *         other invocation of the binding instance. This is crucial because
     *         otherwise they would exist in different contexts.
     */
    private fun createBindings(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        binds = FragmentCalendarBinding.inflate(inflater, container, false)
        return binds.root
    }


    // --- Temporary


    fun insert(event: Event) {
        events.add(event)
        updateCalendarView()
    }


    fun delete(eventId: String) {
        events.removeAll { it.id == eventId }
        updateCalendarView()
    }


    private fun seed() {
        val calendar = Calendar.getInstance()

        // Sample practice event
        calendar.set(2024, 11, 15) // December 15, 2024
        events.add(
            Event(
                id = "1",
                title = "Team Practice",
                type = EventType.PRACTICE,
                date = calendar.time,
                time = "15:00",
                venue = "Main Field",
                createdBy = "Coach"
            )
        )

        // Sample game event
        calendar.set(2024, 11, 22) // December 22, 2024
        events.add(
            Event(
                id = "2",
                title = "MATCH vs Eagles",
                type = EventType.MATCH,
                date = calendar.time,
                time = "14:00",
                venue = "Stadium",
                opponent = "Eagles FC",
                createdBy = "Coach"
            )
        )

        // Sample general event
        calendar.set(2024, 11, 10) // December 10, 2024
        events.add(
            Event(
                id = "3",
                title = "Team Meeting",
                type = EventType.OTHER,
                date = calendar.time,
                time = "18:00",
                venue = "Club House",
                createdBy = "Manager"
            )
        )

        // Add some events for current month
        val currentCalendar = Calendar.getInstance()
        currentCalendar.add(Calendar.DAY_OF_MONTH, 2) // 2 days from now
        events.add(
            Event(
                id = "4",
                title = "Training Session",
                type = EventType.PRACTICE,
                date = currentCalendar.time,
                time = "16:00",
                venue = "Training Ground",
                createdBy = "Coach"
            )
        )

        currentCalendar.add(Calendar.DAY_OF_MONTH, 5) // 7 days from now
        events.add(
            Event(
                id = "5",
                title = "Friendly MATCH",
                type = EventType.MATCH,
                date = currentCalendar.time,
                time = "15:30",
                venue = "Local Stadium",
                opponent = "City Rovers",
                createdBy = "Coach"
            )
        )

        // Apply changes
        updateCalendarView()
    }
} 
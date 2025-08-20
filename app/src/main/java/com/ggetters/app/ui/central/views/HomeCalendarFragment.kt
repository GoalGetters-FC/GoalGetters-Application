package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.databinding.FragmentCalendarBinding
import com.ggetters.app.ui.central.adapters.CalendarAdapter
import com.ggetters.app.ui.central.adapters.EventAdapter
import com.ggetters.app.data.model.CalendarDayItem
import com.ggetters.app.ui.central.sheets.AddEventBottomSheet
import com.ggetters.app.ui.central.sheets.EventDetailsBottomSheet
import com.ggetters.app.ui.central.sheets.EventListBottomSheet
import com.ggetters.app.ui.central.viewmodels.HomeCalendarViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.shared.models.Clickable
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class HomeCalendarFragment : Fragment(), Clickable {
    companion object {
        private const val TAG = "HomeCalendarFragment"
        private const val REQUEST_ADD_EVENT = 1001
    }

    private val activeModel: HomeCalendarViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var binds: FragmentCalendarBinding
    private lateinit var adapter: CalendarAdapter
    private lateinit var eventsAdapter: EventAdapter

    private var currentDate = Calendar.getInstance()
    private val events = mutableListOf<Event>()
    private var selectedDay: Int? = null
    private var selectedDayEvents: List<Event> = emptyList()

    // --- Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = createBindings(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentDate = Calendar.getInstance()

        setupTouchListeners()
        setupCalendar()
        updateCalendarView()

        hideSelectedDayEvents()
        updateMonthYearDisplay()
        autoSelectToday()
    }

    // --- Internals

    private fun offsetCalendarView(months: Int) {
        require(months != 0)
        currentDate.add(Calendar.MONTH, months)
        updateCalendarView()
        animateCalendarTransition()
    }

    private fun incrementCalendarView() = offsetCalendarView(-1)
    private fun decrementCalendarView() = offsetCalendarView(+1)

    private fun animateCalendarTransition() {
        binds.rvCalendar.alpha = 0.5f
        binds.rvCalendar.animate().apply {
            alpha(1.0f)
            duration = 150
        }.start()
    }

    private fun autoSelectToday() {
        val today = Calendar.getInstance()
        val todayEvents = getEventsForDate(today)
        val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        if (currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        ) {
            selectedDay = todayDayOfMonth
            showSelectedDayEvents(todayDayOfMonth, todayEvents)
        }

        updateCalendarView()
    }

    private fun updateCalendarView() {
        val calendarDays = generateCalendarDays()
        adapter = CalendarAdapter(
            onClick = { day -> onDayClickedCallback(day) },
            onLongClick = { day -> onDayLongClickedCallback(day) }
        )
        binds.rvCalendar.adapter = adapter
        adapter.update(calendarDays)
        updateMonthYearDisplay()
    }

    private fun generateCalendarDays(): List<CalendarDayItem> {
        val days = mutableListOf<CalendarDayItem>()
        val calendar = currentDate.clone() as Calendar
        val today = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - 1

        for (i in 0 until startOffset) {
            days.add(CalendarDayItem(isCurrentMonth = false))
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dayCalendar = currentDate.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)

            val dayEvents = getEventsForDate(dayCalendar)

            val isToday = isSameDay(dayCalendar, today)
            val isSelected = selectedDay == day

            days.add(
                CalendarDayItem(
                    dayNumber = day,
                    events = dayEvents,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
        }

        return days
    }

    private fun isSameDay(oldCalendar: Calendar, newCalendar: Calendar): Boolean {
        val oldDate = LocalDate.of(
            oldCalendar.get(Calendar.YEAR),
            oldCalendar.get(Calendar.MONTH) + 1,
            oldCalendar.get(Calendar.DAY_OF_MONTH)
        )

        val newDate = LocalDate.of(
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH) + 1,
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )

        return oldDate.isEqual(newDate)
    }

    private fun getEventsForDate(calendar: Calendar): List<Event> {
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
        val targetMonth = calendar.get(Calendar.MONTH) + 1
        val targetYear = calendar.get(Calendar.YEAR)

        return events.filter { event ->
            val eventDate = event.startAt.toLocalDate()
            eventDate.dayOfMonth == targetDay &&
                    eventDate.monthValue == targetMonth &&
                    eventDate.year == targetYear
        }.sortedBy { event ->
            event.startAt.toLocalTime()
        }
    }

    private fun onDayClickedCallback(dayOfMonth: Int) {
        selectedDay = dayOfMonth
        updateCalendarView()

        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        selectedDayEvents = getEventsForDate(calendar)

        showSelectedDayEvents(dayOfMonth, selectedDayEvents)
    }

    private fun onDayLongClickedCallback(dayOfMonth: Int) {
        showAddEventBottomSheet(dayOfMonth)
    }

    private fun showSelectedDayEvents(dayOfMonth: Int, events: List<Event>) {
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        binds.selectedDateText.text = dateFormat.format(calendar.time)

        if (events.isEmpty()) {
            binds.rvSelectedDayEvents.visibility = View.GONE
            binds.noEventsLayout.visibility = View.VISIBLE
        } else {
            binds.rvSelectedDayEvents.visibility = View.VISIBLE
            binds.noEventsLayout.visibility = View.GONE
            eventsAdapter.update(events)
        }

        binds.selectedDayEventsSection.visibility = View.VISIBLE
    }

    private fun hideSelectedDayEvents() {
        binds.rvSelectedDayEvents.visibility = View.GONE
        binds.noEventsLayout.visibility = View.GONE
    }

    private fun showEventDetails(event: Event) {
        if (event.category == EventCategory.MATCH) {
            val intent = Intent(requireContext(), MatchActivity::class.java).apply {
                putExtra("event_id", event.id)
                putExtra("event_title", event.name)
                putExtra("event_venue", event.location ?: "TBD")
                putExtra("event_opponent", event.description ?: "Opponent")
                putExtra("event_date", event.startAt.toLocalDate().toString())
                putExtra("event_time", event.startAt.toLocalTime().toString())
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        } else {
            val eventDetailsSheet = EventDetailsBottomSheet.newInstance(event)
            eventDetailsSheet.show(childFragmentManager, "EventDetailsBottomSheet")
        }
    }

    private fun editEvent(event: Event) {
        showEventDetails(event)
    }

    private fun showEventsForDay(day: Int, events: List<Event>) {
        val eventListBottomSheet = EventListBottomSheet.newInstance(day, events)
        eventListBottomSheet.show(childFragmentManager, "EventListBottomSheet")
    }

    private fun showAddEventBottomSheet(selectedDay: Int? = null) {
        val intent = Intent(requireContext(), AddEventActivity::class.java)
        if (selectedDay != null) {
            val calendar = currentDate.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            intent.putExtra("selected_date", calendar.timeInMillis)
        }
        startActivityForResult(intent, REQUEST_ADD_EVENT)
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
            Clogger.w(TAG, "Unhandled on-click for: ${view?.id}")
        }
    }

    // --- UI Configuration

    private fun setupCalendar() {
        binds.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = CalendarAdapter(
                onClick = { day -> onDayClickedCallback(day) },
                onLongClick = { day -> onDayLongClickedCallback(day) }
            )
        }

        eventsAdapter = EventAdapter(
            onClick = { event -> showEventDetails(event) },
            onLongClick = { event -> editEvent(event) }
        )
        binds.rvSelectedDayEvents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }

        setupSwipeGestures()
    }

    private fun setupSwipeGestures() {
        binds.rvCalendar.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
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
                        if (abs(deltaX) > abs(deltaY) && abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0) incrementCalendarView() else decrementCalendarView()
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

    private fun createBindings(inflater: LayoutInflater, container: ViewGroup?): View {
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

    private fun updateMonthYearDisplay() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binds.monthYearText.text = dateFormat.format(currentDate.time)
    }
}

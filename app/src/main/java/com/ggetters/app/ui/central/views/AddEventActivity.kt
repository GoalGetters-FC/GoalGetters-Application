package com.ggetters.app.ui.central.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import com.ggetters.app.ui.central.fragments.GameEventFragment
import com.ggetters.app.ui.central.fragments.PracticeEventFragment
import com.ggetters.app.ui.central.fragments.GeneralEventFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddEventActivity : AppCompatActivity() {

    private lateinit var eventTypeViewPager: androidx.viewpager2.widget.ViewPager2
    private lateinit var eventTypeTabLayout: com.google.android.material.tabs.TabLayout
    private lateinit var scheduleButton: android.widget.Button
    private lateinit var closeButton: android.widget.ImageButton

    private var selectedDate: Date? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var selectedMeetingTime: String? = null
    private var onEventCreatedListener: ((Event) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        setupViews()
        setupViewPager()
        setupTabLayout()
        setupClickListeners()
        
        // Set default date if provided from intent
        intent.getLongExtra("selected_date", -1).takeIf { it != -1L }?.let { timestamp ->
            selectedDate = Date(timestamp)
        }
    }

    private fun setupViews() {
        eventTypeViewPager = findViewById(R.id.eventTypeViewPager)
        eventTypeTabLayout = findViewById(R.id.eventTypeTabLayout)
        scheduleButton = findViewById(R.id.scheduleButton)
        closeButton = findViewById(R.id.closeButton)
    }

    private fun setupViewPager() {
        eventTypeViewPager.adapter = EventTypePagerAdapter(this)
    }

    private fun setupTabLayout() {
        TabLayoutMediator(eventTypeTabLayout, eventTypeViewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Game"
                    tab.setIcon(R.drawable.ic_unicons_trophy_24)
                }
                1 -> {
                    tab.text = "Practice"
                    tab.setIcon(R.drawable.ic_unicons_whistle_24)
                }
                2 -> {
                    tab.text = "Event"
                    tab.setIcon(R.drawable.ic_unicons_calendar_24)
                }
            }
        }.attach()
    }

    private fun setupClickListeners() {
        closeButton.setOnClickListener {
            finish()
        }

        scheduleButton.setOnClickListener {
            if (validateForm()) {
                val event = createEvent()
                onEventCreatedListener?.invoke(event)
                finish()
            }
        }
    }

    private fun validateForm(): Boolean {
        // TODO: Implement form validation based on current tab
        return true
    }

    private fun createEvent(): Event {
        val currentTab = eventTypeTabLayout.selectedTabPosition
        val eventType = when (currentTab) {
            0 -> EventType.MATCH
            1 -> EventType.PRACTICE
            2 -> EventType.OTHER
            else -> EventType.PRACTICE
        }

        // TODO: Extract form data from current fragment
        return Event(
            id = UUID.randomUUID().toString(),
            title = "New Event",
            type = eventType,
            date = selectedDate ?: Date(),
            time = selectedStartTime ?: "",
            venue = "Location",
            opponent = if (eventType == EventType.MATCH) "Opponent" else null,
            description = "Description",
            createdBy = "Current User"
        )
    }

    fun setOnEventCreatedListener(listener: (Event) -> Unit) {
        onEventCreatedListener = listener
    }

    // ViewPager Adapter
    private inner class EventTypePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GameEventFragment()
                1 -> PracticeEventFragment()
                2 -> GeneralEventFragment()
                else -> PracticeEventFragment()
            }
        }
    }
} 
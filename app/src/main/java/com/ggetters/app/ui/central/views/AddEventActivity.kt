package com.ggetters.app.ui.central.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.ui.central.fragments.GameEventFragment
import com.ggetters.app.ui.central.fragments.PracticeEventFragment
import com.ggetters.app.ui.central.fragments.GeneralEventFragment
import com.ggetters.app.ui.central.viewmodels.EventFormPickers
import com.ggetters.app.ui.central.viewmodels.EventUpsertViewModel
import com.ggetters.app.ui.central.models.UpsertState
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEventActivity : AppCompatActivity() {

    private val upsertVm: EventUpsertViewModel by viewModels()
    private lateinit var eventTypeViewPager: androidx.viewpager2.widget.ViewPager2
    private lateinit var eventTypeTabLayout: com.google.android.material.tabs.TabLayout
    private lateinit var scheduleButton: android.widget.Button
    private lateinit var closeButton: android.widget.ImageButton
    
    // Store fragment references
    private val gameFragment = GameEventFragment()
    private val practiceFragment = PracticeEventFragment()
    private val generalFragment = GeneralEventFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        setupViews()
        setupViewPager()
        setupTabLayout()
        setupClickListeners()
        observeSaves()
    }

    private fun observeSaves() {
        lifecycleScope.launch {
            upsertVm.state.collect { st ->
                Clogger.i("AddEventActivity", "ViewModel state = $st")
                when (st) {
                    is UpsertState.Error ->
                        Snackbar.make(scheduleButton, st.reason, Snackbar.LENGTH_LONG).show()
                    is UpsertState.Saved -> {
                        Snackbar.make(scheduleButton, "Event scheduled!", Snackbar.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                    else -> {}
                }
            }
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
                0 -> { tab.text = "Game"; tab.setIcon(R.drawable.ic_unicons_trophy_24) }
                1 -> { tab.text = "Practice"; tab.setIcon(R.drawable.ic_unicons_whistle_24) }
                2 -> { tab.text = "Event"; tab.setIcon(R.drawable.ic_unicons_calendar_24) }
            }
        }.attach()
    }

    private fun setupClickListeners() {
        closeButton.setOnClickListener { 
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        scheduleButton.setOnClickListener {
            Clogger.i("AddEventActivity", "Schedule button clicked")

            val currentItem = eventTypeViewPager.currentItem
            Clogger.i("AddEventActivity", "Current tab position: $currentItem")
            
            // Get the current fragment based on tab position
            val fragment = when (currentItem) {
                0 -> gameFragment
                1 -> practiceFragment
                2 -> generalFragment
                else -> practiceFragment
            }
            
            Clogger.i("AddEventActivity", "Using fragment: ${fragment.javaClass.simpleName}")

            val formData = when (fragment) {
                is GameEventFragment -> fragment.collectFormData()
                is PracticeEventFragment -> fragment.collectFormData()
                is GeneralEventFragment -> fragment.collectFormData()
                else -> null
            }

            if (formData == null) {
                Clogger.e("AddEventActivity", "❌ collectFormData() returned null - validation failed")
                Snackbar.make(scheduleButton, "Please fill in all required fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Clogger.i("AddEventActivity", "✅ Collected formData=$formData")

            val startAt = EventFormPickers.combine(formData.date, formData.start)
            val endAt   = EventFormPickers.combine(formData.date, formData.end)
            val meetAt  = EventFormPickers.combine(formData.date, formData.meet)

            val category = when (eventTypeTabLayout.selectedTabPosition) {
                0 -> EventCategory.MATCH
                1 -> EventCategory.PRACTICE
                else -> EventCategory.OTHER
            }

            Clogger.i("AddEventActivity", "formData.date=${formData.date}, start=${formData.start}, end=${formData.end}, meet=${formData.meet}")
            upsertVm.save(
                category = category,
                title = formData.title,
                location = formData.location,
                description = formData.description,
                startAt = startAt,
                endAt = endAt,
                meetingAt = meetAt
            )
        }
    }


    private inner class EventTypePagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> gameFragment
            1 -> practiceFragment
            2 -> generalFragment
            else -> practiceFragment
        }
    }
}
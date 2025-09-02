package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ggetters.app.R
import com.ggetters.app.data.model.Event
import com.ggetters.app.ui.central.fragments.AttendanceFragment
import com.ggetters.app.ui.central.fragments.LineupFragment
import com.ggetters.app.ui.central.fragments.MatchDetailsFragment
import com.ggetters.app.ui.central.viewmodels.MatchViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MatchActivity : AppCompatActivity() {

    private val viewModel: MatchViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var headerDate: TextView

    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)

        eventId = intent.getStringExtra("event_id") ?: ""

        setupViews()
        setupTabs()

        viewModel.loadEvent(eventId)
        observeEvent()
    }


    private fun setupViews() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        headerDate = findViewById(R.id.headerDate)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun observeEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.event.collectLatest { event ->
                event?.let { updateHeader(it) }
            }
        }
    }

    private fun updateHeader(event: Event) {
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val instant = event.startAt.atZone(java.time.ZoneId.systemDefault()).toInstant()
        val dateText = formatter.format(Date.from(instant))
        headerDate.text = dateText
    }


    private fun setupTabs() {
        val adapter = MatchPagerAdapter(this, eventId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Details"
                1 -> "Attendance"
                2 -> "Lineup"
                else -> "Tab"
            }
            tab.icon = when (position) {
                0 -> getDrawable(R.drawable.ic_unicons_info_circle_24)
                1 -> getDrawable(R.drawable.ic_unicons_users_24)
                2 -> getDrawable(R.drawable.ic_unicons_soccer_24)
                else -> null
            }
        }.attach()
        viewPager.setCurrentItem(0, false)
    }

    private inner class MatchPagerAdapter(
        activity: AppCompatActivity,
        private val eventId: String
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int) = when (position) {
            0 -> MatchDetailsFragment.newInstance(eventId)
            1 -> AttendanceFragment.newInstance(eventId)
            2 -> LineupFragment.newInstance(eventId)
            else -> throw IllegalStateException("Invalid tab position: $position")
        }
    }
}

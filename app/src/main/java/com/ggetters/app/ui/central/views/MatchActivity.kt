package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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

        // Setup status bar for dark header
        setupStatusBar()

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


    private fun setupStatusBar() {
        // Enable edge-to-edge display but keep status bar visible
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set up window insets controller for dark status bar (since we have dark header)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // Dark status bar icons for dark background

        // Set status bar to match the dark header color
        window.statusBarColor = android.graphics.Color.parseColor("#161620")
    }

    private fun setupTabs() {
        val adapter = MatchPagerAdapter(this, eventId)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> { 
                    tab.text = "Details"
                    tab.setIcon(R.drawable.ic_details_modern_24)
                }
                1 -> { 
                    tab.text = "Attendance"
                    tab.setIcon(R.drawable.ic_attendance_modern_24)
                }
                2 -> { 
                    tab.text = "Lineup"
                    tab.setIcon(R.drawable.ic_lineup_modern_24)
                }
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

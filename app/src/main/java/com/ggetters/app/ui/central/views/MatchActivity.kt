package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ggetters.app.R
import com.ggetters.app.ui.central.fragments.AttendanceFragment
import com.ggetters.app.ui.central.fragments.LineupFragment
import com.ggetters.app.ui.central.fragments.MatchDetailsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

// TODO: Backend - Implement real-time match data synchronization across all tabs
// TODO: Backend - Add match data caching for offline access
// TODO: Backend - Implement permission-based UI (coach vs player views)
// TODO: Backend - Add push notifications for match updates

@AndroidEntryPoint
class MatchActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var headerDate: TextView
    
    // Match data from intent
    private var matchId: String = ""
    private var matchTitle: String = ""
    private var matchDate: Long = 0L
    private var homeTeam: String = ""
    private var awayTeam: String = ""
    private var venue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)
        
        loadMatchDataFromIntent()
        setupViews()
        setupTabs()
    }

    private fun loadMatchDataFromIntent() {
        matchId = intent.getStringExtra("match_id") ?: ""
        matchTitle = intent.getStringExtra("match_title") ?: "Match"
        matchDate = intent.getLongExtra("match_date", System.currentTimeMillis())
        homeTeam = intent.getStringExtra("home_team") ?: "Home Team"
        awayTeam = intent.getStringExtra("away_team") ?: "Away Team"
        venue = intent.getStringExtra("venue") ?: "Stadium"
    }

    private fun setupViews() {
        // Header setup
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
        
        headerDate = findViewById(R.id.headerDate)
        headerDate.text = formatMatchDate(matchDate)
        
        // ViewPager and TabLayout
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun formatMatchDate(dateMillis: Long): String {
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(dateMillis))
    }

    private fun setupTabs() {
        val adapter = MatchPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Details"
                1 -> "Attendance"
                2 -> "Lineup"
                else -> "Tab"
            }
            
            // Add icons to tabs
            tab.icon = when (position) {
                0 -> getDrawable(R.drawable.ic_unicons_info_circle_24)
                1 -> getDrawable(R.drawable.ic_unicons_users_24)
                2 -> getDrawable(R.drawable.ic_unicons_soccer_24)
                else -> null
            }
        }.attach()
        
        // Set default tab to Details
        viewPager.setCurrentItem(0, false)
    }

    private inner class MatchPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MatchDetailsFragment.newInstance(
                    matchId = matchId,
                    matchTitle = matchTitle,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    venue = venue,
                    matchDate = matchDate
                )
                1 -> AttendanceFragment.newInstance(
                    matchId = matchId,
                    matchTitle = matchTitle
                )
                2 -> LineupFragment.newInstance(
                    matchId = matchId,
                    matchTitle = matchTitle,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam
                )
                else -> throw IllegalStateException("Invalid tab position: $position")
            }
        }
    }

    // Public methods for fragments to access match data
    fun getMatchData(): Map<String, Any> {
        return mapOf(
            "match_id" to matchId,
            "match_title" to matchTitle,
            "match_date" to matchDate,
            "home_team" to homeTeam,
            "away_team" to awayTeam,
            "venue" to venue
        )
    }

    // Method to switch tabs programmatically
    fun switchToTab(tabIndex: Int) {
        if (tabIndex in 0..2) {
            viewPager.setCurrentItem(tabIndex, true)
        }
    }
}

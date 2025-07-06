package com.ggetters.app.ui.central.views

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ggetters.app.R

// TODO: Backend - Fetch data for each tab (Notifications, Calendar, Players, Team Profile)
// TODO: Backend - Log analytics for tab navigation

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BadgeBottomNavigationView
    private var currentFragment: Fragment? = null
    private var longPressStartTime: Long = 0
    private val LONG_PRESS_DURATION = 500L // 500ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupBottomNavigation()
        checkUnreadNotifications()
        
        // Set default fragment
        if (savedInstanceState == null) {
            switchFragment(CalendarFragment())
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        
        // Set up item selection listener
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val newFragment = when (menuItem.itemId) {
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_players -> PlayersFragment()
                R.id.nav_team_profile -> TeamProfileFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }
            
            if (newFragment != null) {
                switchFragment(newFragment)
            }
            true
        }
        
        // Set up long press detection for profile tab
        bottomNavigationView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressStartTime = System.currentTimeMillis()
                    false
                }
                MotionEvent.ACTION_UP -> {
                    val pressDuration = System.currentTimeMillis() - longPressStartTime
                    if (pressDuration >= LONG_PRESS_DURATION) {
                        // Check if we're pressing on the profile tab
                        val profileTab = bottomNavigationView.findViewById<View>(R.id.nav_profile)
                        if (profileTab != null && isTouchInView(event, profileTab)) {
                            Log.d("HomeActivity", "Long press detected on profile tab")
                            showAccountSwitcher()
                            return@setOnTouchListener true
                        }
                    }
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val pressDuration = System.currentTimeMillis() - longPressStartTime
                    if (pressDuration >= LONG_PRESS_DURATION) {
                        // Check if we're still pressing on the profile tab
                        val profileTab = bottomNavigationView.findViewById<View>(R.id.nav_profile)
                        if (profileTab != null && isTouchInView(event, profileTab)) {
                            return@setOnTouchListener true
                        }
                    }
                    false
                }
            }
            false
        }
        
        // Alternative approach: Add long click listener after view is laid out
        bottomNavigationView.post {
            val profileTab = bottomNavigationView.findViewById<View>(R.id.nav_profile)
            Log.d("HomeActivity", "Profile tab found: ${profileTab != null}")
            profileTab?.setOnLongClickListener {
                Log.d("HomeActivity", "Profile tab long clicked")
                showAccountSwitcher()
                true
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        
        // Add smooth transitions
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        
        if (currentFragment == null) {
            transaction.replace(R.id.fragmentContainer, fragment)
        } else {
            transaction.replace(R.id.fragmentContainer, fragment)
        }
        
        transaction.commit()
        currentFragment = fragment
    }

    private fun showAccountSwitcher() {
        Log.d("HomeActivity", "Showing account switcher from HomeActivity")
        
        // Get the current ProfileFragment to show the account switcher
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is ProfileFragment) {
            currentFragment.showAccountSwitcher()
        } else {
            Log.e("HomeActivity", "Current fragment is not ProfileFragment")
        }
    }

    private fun checkUnreadNotifications() {
        // TODO: Backend - Fetch unread notifications count
        // Endpoint: GET /api/notifications/unread/count
        // Request: { userId: String }
        // Response: { count: number }
        // Error handling: { message: String, code: String }
        
        // For now, using sample data
        val hasUnreadNotifications = true // This should come from backend
        bottomNavigationView.showNotificationBadge(hasUnreadNotifications)
    }

    private fun isTouchInView(event: MotionEvent, view: View): Boolean {
        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)
        return viewRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
} 
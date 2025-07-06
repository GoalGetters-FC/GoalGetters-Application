package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ggetters.app.R

// TODO: Backend - Fetch data for each tab (Notifications, Calendar, Players, Team Profile)
// TODO: Backend - Log analytics for tab navigation

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BadgeBottomNavigationView
    private var currentFragment: Fragment? = null

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
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val newFragment = when (menuItem.itemId) {
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_players -> {
                    // TODO: Show players fragment
                    null
                }
                R.id.nav_team_profile -> {
                    // TODO: Show team profile fragment
                    null
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    null
                }
                else -> null
            }
            
            if (newFragment != null) {
                switchFragment(newFragment)
            }
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment == null) {
            transaction.replace(R.id.fragmentContainer, fragment)
        } else {
            transaction.setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.slide_out_right
            )
            transaction.replace(R.id.fragmentContainer, fragment)
        }
        transaction.commit()
        currentFragment = fragment
    }

    private fun checkUnreadNotifications() {
        // TODO: Backend - Fetch unread notifications count
        // For now, using sample data
        val hasUnreadNotifications = true // This should come from backend
        bottomNavigationView.showNotificationBadge(hasUnreadNotifications)
    }
} 
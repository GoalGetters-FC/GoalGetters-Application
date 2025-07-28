package com.ggetters.app.ui.central.views

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityHomeBinding
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: Backend - Fetch data for each tab (Notifications, Calendar, Players, Team Profile)
// TODO: Backend - Log analytics for tab navigation

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HomeActivity"
        private const val LONG_PRESS_DURATION_MS = 500L
    }


    private lateinit var binds: ActivityHomeBinding
    private val model: HomeViewModel by viewModels()


    private var currentFragment: Fragment? = null
    private var longPressStartTime: Long = 0


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()

        setupStatusBar()
        setupViews()
        setupBottomNavigation()
        checkUnreadNotifications()

        // Load default fragment
        if (savedInstanceState == null) {
            switchFragment(HomeCalendarFragment())
        }

        observe()
    }


// --- ViewModel


    // TODO
    private fun observe() {}


// --- Internals


    private fun setupStatusBar() {
        // Enable edge-to-edge display but keep status bar visible
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set up window insets controller for light status bar
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true // Light status bar icons

        // Ensure status bar is visible and properly colored
        window.statusBarColor = getColor(R.color.white)
    }


    private fun setupViews() {
        binds.notificationsIcon.setOnClickListener {
            Clogger.d(
                TAG, "Notifications icon clicked"
            )
            
            // Launch NotificationsActivity
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
    }


    private fun setupBottomNavigation() {
        binds.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val newFragment = when (menuItem.itemId) {
                R.id.nav_calendar -> HomeCalendarFragment()
                R.id.nav_team_players -> HomePlayersFragment()
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
        binds.bottomNavigationView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressStartTime = System.currentTimeMillis()
                    false
                }

                MotionEvent.ACTION_UP -> {
                    val pressDuration = System.currentTimeMillis() - longPressStartTime
                    if (pressDuration >= LONG_PRESS_DURATION_MS) {
                        // Check if we're pressing on the profile tab
                        val profileTab =
                            binds.bottomNavigationView.findViewById<View>(R.id.nav_profile)
                        if (profileTab != null && isTouchInView(event, profileTab)) {
                            Clogger.d(
                                TAG, "Long press detected on profile tab"
                            )

                            showAccountSwitcher()
                            return@setOnTouchListener true
                        }
                    }
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val pressDuration = System.currentTimeMillis() - longPressStartTime
                    if (pressDuration >= LONG_PRESS_DURATION_MS) {
                        // Check if we're still pressing on the profile tab
                        val profileTab =
                            binds.bottomNavigationView.findViewById<View>(R.id.nav_profile)
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
        binds.bottomNavigationView.post {
            val profileTab = binds.bottomNavigationView.findViewById<View>(R.id.nav_profile)
            Clogger.d(
                TAG, "Profile tab found: ${profileTab != null}"
            )

            profileTab?.setOnLongClickListener {
                Clogger.d(
                    TAG, "Profile tab long clicked"
                )

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
        Clogger.d(
            TAG, "Showing account switcher from HomeActivity"
        )

        // Get the current ProfileFragment to show the account switcher
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is ProfileFragment) {
            currentFragment.showAccountSwitcher()
        } else {
            Clogger.e(
                TAG, "Current fragment is not ProfileFragment"
            )
        }
    }


    private fun checkUnreadNotifications() {
        val hasUnreadNotifications = true
        showNotificationBadge(hasUnreadNotifications)
        binds.bottomNavigationView.showNotificationBadge(hasUnreadNotifications)
    }


    private fun showNotificationBadge(show: Boolean) {
        binds.notificationBadge.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun toggleNotificationBadge() {
        val isVisible = binds.notificationBadge.isVisible
        showNotificationBadge(!isVisible)
    }


    private fun isTouchInView(event: MotionEvent, view: View): Boolean {
        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)
        return viewRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }


// --- UI


    private fun setupBindings() {
        binds = ActivityHomeBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()

        // Apply system-bar insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
} 
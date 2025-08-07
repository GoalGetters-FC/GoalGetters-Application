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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamsFragment

// TODO: Backend - Fetch data for each tab (Notifications, Calendar, Players, Team Profile)
// TODO: Backend - Log analytics for tab navigation
// TODO: Backend - Implement real-time notification badge updates
// TODO: Backend - Add analytics tracking for tab navigation and user interactions
// TODO: Backend - Implement proper user session management and authentication state
// TODO: Backend - Add offline/online state handling for data synchronization
// TODO: Backend - Implement push notification handling and badge management
// TODO: Backend - Add proper error handling and retry mechanisms for network operations

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
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED // Ensure labels are always visible

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_calendar -> {
                    switchFragment(HomeCalendarFragment())
                    true
                }
                R.id.nav_team_players -> { // Corrected ID
                    switchFragment(HomePlayersFragment())
                    true
                }
                R.id.nav_player_profile -> { // Player Profile - Show player details
                    switchFragment(PlayerDetailsFragment())
                    true
                }
                R.id.nav_profile -> {
                    // Options tab - Show account switcher on long press, regular click shows profile
                    switchFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Add custom touch listener for long press functionality
        var longPressHandler: android.os.Handler? = null
        var longPressRunnable: Runnable? = null
        
        bottomNav.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    val touchedItem = getBottomNavItemAtLocation(bottomNav, event.x, event.y)
                    if (touchedItem == R.id.nav_player_profile || touchedItem == R.id.nav_profile) {
                        // Start a timer for long press - both player profile and options should show account switcher
                        longPressRunnable = Runnable {
                            showAccountSwitcher()
                        }
                        longPressHandler = android.os.Handler(android.os.Looper.getMainLooper())
                        longPressHandler?.postDelayed(longPressRunnable!!, 500) // 500ms for long press
                    }
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    // Cancel long press if touch is released
                    longPressHandler?.removeCallbacksAndMessages(null)
                    longPressHandler = null
                    longPressRunnable = null
                }
            }
            false // Don't consume the event
        }
        
        switchFragment(HomeCalendarFragment()) // Set default fragment
    }

    private fun getBottomNavItemAtLocation(bottomNav: BottomNavigationView, x: Float, y: Float): Int? {
        // Simple implementation - you might need to refine this based on your needs
        val menu = bottomNav.menu
        val itemCount = menu.size()
        val itemWidth = bottomNav.width / itemCount
        
        val itemIndex = (x / itemWidth).toInt()
        return if (itemIndex in 0 until itemCount) {
            menu.getItem(itemIndex).itemId
        } else null
    }
    
    private fun showTeamSwitcher() {
        // TODO: Backend - Implement team switching with proper authentication
        // TODO: Backend - Add team switching analytics and tracking
        // TODO: Backend - Implement team switching notifications and confirmations
        // TODO: Backend - Add team switching validation and permissions
        // TODO: Backend - Implement team switching data synchronization

        TeamSwitcherBottomSheet.newInstance(
            onTeamSelected = { selectedTeam ->
                // Handle team selection
                Snackbar.make(findViewById(android.R.id.content), "Switched to ${selectedTeam.teamName}", Snackbar.LENGTH_SHORT).show()
                // TODO: Backend - Update current team in backend
                // TODO: Backend - Refresh all fragments with new team data
                // TODO: Backend - Update team-specific data across the app
            },
            onManageTeams = {
                // Navigate to team management
                switchFragment(TeamsFragment())
            },
            onSetDefaultTeam = { teamId ->
                // Handle default team setting
                setDefaultTeam(teamId)
            }
        ).show(supportFragmentManager, "TeamSwitcher")
    }
    
    private fun setDefaultTeam(teamId: String) {
        // TODO: Backend - Save default team preference to backend
        // TODO: Backend - Implement default team validation
        // TODO: Backend - Add default team analytics
        // TODO: Backend - Implement default team notifications
        // TODO: Backend - Add default team data synchronization
        
        Snackbar.make(findViewById(android.R.id.content), "Default team updated", Snackbar.LENGTH_SHORT).show()
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        // Add smooth transitions with better timing
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )

        // Add to back stack for proper navigation
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)

        transaction.commit()
        currentFragment = fragment
    }

    fun showAccountSwitcher() {
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
        // TODO: Backend - Fetch real unread notification count from backend
        // TODO: Backend - Implement real-time updates using WebSocket or polling
        // TODO: Backend - Add notification preferences and filtering
        val hasUnreadNotifications = true
        showNotificationBadge(hasUnreadNotifications)
        //binds.bottomNavigationView.showNotificationBadge(hasUnreadNotifications)
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
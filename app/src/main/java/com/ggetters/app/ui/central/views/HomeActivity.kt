package com.ggetters.app.ui.central.views

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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
        // Notifications are handled through the toolbar menu
        // The menu item with id menu_home_notifications will handle the click
    }


    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED // Ensure labels are always visible

        // Ignore re-selections to avoid jitter
        bottomNav.setOnItemReselectedListener { /* no-op */ }

        bottomNav.setOnItemSelectedListener { menuItem ->
            val currentId = when (currentFragment) {
                is HomeCalendarFragment -> R.id.nav_calendar
                is HomeTeamFragment -> R.id.nav_team_players
                is PlayerProfileFragment -> R.id.nav_player_profile
                is ProfileFragment -> R.id.nav_profile
                else -> R.id.nav_calendar
            }

            val newIndex = navIndexFor(menuItem.itemId)
            val oldIndex = navIndexFor(currentId)
            val isForward = newIndex >= oldIndex

            when (menuItem.itemId) {
                R.id.nav_calendar -> switchFragment(HomeCalendarFragment(), isForward)
                R.id.nav_team_players -> switchFragment(HomeTeamFragment(), isForward)
                R.id.nav_player_profile -> {
                    // Launch UserProfileActivity for user profile
                    val intent = Intent(this, UserProfileActivity::class.java).apply {
                        putExtra(UserProfileActivity.EXTRA_PROFILE_TYPE, UserProfileActivity.PROFILE_TYPE_USER)
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> switchFragment(ProfileFragment(), isForward)
                else -> false
            }.let { handled -> handled }
        }

        // Setup long click listener for Options tab specifically
        setupOptionsLongClick(bottomNav)


        
        switchFragment(HomeCalendarFragment()) // Set default fragment
    }

    private fun setupOptionsLongClick(bottomNav: BottomNavigationView) {
        // Post to ensure the bottom navigation is fully laid out
        bottomNav.post {
            try {
                // Find the Options tab view using reflection (similar to profile avatar approach)
                val menuView = bottomNav.getChildAt(0) as? ViewGroup
                if (menuView != null) {
                    // The Options tab is the 4th item (index 3)
                    val optionsItemView = menuView.getChildAt(3)
                    
                    if (optionsItemView != null) {
                        // Set long click listener directly on the Options tab view
                        optionsItemView.setOnLongClickListener { view ->
                            Clogger.d(TAG, "Options tab long-press detected!")
                            android.util.Log.d(TAG, "🎯 Options long press working!")
                            
                            // Add haptic feedback (same as profile avatar)
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            
                            // Show account switcher
                            showAccountSwitcher()
                            true // Consume the event
                        }
                        
                        Clogger.d(TAG, "Successfully set long click listener on Options tab")
                    } else {
                        Clogger.e(TAG, "Could not find Options tab view")
                    }
                } else {
                    Clogger.e(TAG, "Could not find bottom navigation menu view")
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Error setting up Options long click: ${e.message}")
                
                // Fallback: Try alternative approach
                setupOptionsLongClickFallback(bottomNav)
            }
        }
    }
    
    private fun setupOptionsLongClickFallback(bottomNav: BottomNavigationView) {
        // Alternative approach: Use touch listener but only for Options tab area
        bottomNav.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                // Calculate if touch is in Options tab area (rightmost 25% of bottom nav)
                val optionsAreaStart = view.width * 0.75f
                if (event.x >= optionsAreaStart) {
                    // Schedule long press check
                    view.postDelayed({
                        if (view.isPressed) {
                            Clogger.d(TAG, "Options area long press detected (fallback)")
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            showAccountSwitcher()
                        }
                    }, 600)
                }
            }
            false // Don't consume normal events
        }
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

    private fun switchFragment(fragment: Fragment, isForward: Boolean = true): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        if (isForward) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
        transaction.replace(R.id.fragmentContainer, fragment)
        // Do not add to back stack for primary tabs to prevent deep stacks
        transaction.commit()
        currentFragment = fragment
        return true
    }

    private fun navIndexFor(itemId: Int): Int = when (itemId) {
        R.id.nav_calendar -> 0
        R.id.nav_team_players -> 1
        R.id.nav_player_profile -> 2
        R.id.nav_profile -> 3
        else -> 0
    }

    // Allow fragments to switch tabs without triggering back press exits
    fun navigateToTab(itemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = itemId
    }

    fun showAccountSwitcher() {
        Clogger.d(TAG, "🔄 Account Switcher: Starting to show account switcher")
        
        // Also log to system for debugging
        android.util.Log.d(TAG, "🔄 Account Switcher: Method called")

        // Show account switcher directly from HomeActivity - works from any tab
        val availableAccounts = listOf(
            com.ggetters.app.ui.central.models.UserAccount(
                "1", 
                "Matthew Pieterse", 
                "matthew@example.com",
                null, 
                "U15a Football", 
                "Coach", 
                true
            ),
            com.ggetters.app.ui.central.models.UserAccount(
                "2", 
                "Matthew Pieterse", 
                "matthew@example.com",
                null, 
                "City FC", 
                "Coach", 
                false
            ),
            com.ggetters.app.ui.central.models.UserAccount(
                "3", 
                "John Smith", 
                "john@example.com",
                null, 
                "United FC", 
                "Player", 
                false
            )
        )

        com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
            .newInstance(availableAccounts) { selectedAccount ->
                // TODO: Backend - Call backend to switch active team
                // teamRepo.switchActiveTeam(selectedAccount.id)
                com.google.android.material.snackbar.Snackbar.make(
                    findViewById(android.R.id.content), 
                    "Switched to ${selectedAccount.teamName}", 
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                
                // Refresh current fragment if it's ProfileFragment
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (currentFragment is ProfileFragment) {
                    // Refresh profile with new team data
                    currentFragment.onResume()
                }
            }
            .show(supportFragmentManager, "AccountSwitcher")
    }


    private fun checkUnreadNotifications() {
        // TODO: Backend - Fetch real unread notification count from backend
        // TODO: Backend - Implement real-time updates using WebSocket or polling
        // TODO: Backend - Add notification preferences and filtering
        // Notifications are handled through the toolbar menu
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
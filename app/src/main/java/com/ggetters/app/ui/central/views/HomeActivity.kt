package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.sync.SyncScheduler
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.databinding.ActivityHomeBinding
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.central.views.AccountFragment
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private var currentTabIndex: Int = 0 // Track current tab position for transitions
    private var lastBottomNavClickAt: Long = 0L

    var notificationBadge: ImageView? = null

    // Inject repositories (make sure they’re bound in Hilt)
    @Inject lateinit var teamRepo: TeamRepository
    @Inject lateinit var userRepo: UserRepository
    @Inject lateinit var eventRepo: EventRepository
    @Inject lateinit var attendanceRepo: AttendanceRepository

    private var syncScheduled = false

    // --- Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
        setupLayoutUi()
        setupStatusBar()
        setupViews()
        setupBottomNavigation()

        // Register modern back navigation callback
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null) {
            switchFragmentWithDirection(HomeCalendarFragment(), 0)
        }

        observe()
        observeActiveTeam() // <- safe version

        model.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.WHITE,
                appBarTitle = "August 2025",
                appBarShown = true,
            )
        )
    }

    // --- Observe active team safely
    private fun observeActiveTeam() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                teamRepo.getActiveTeam().collect { team ->
                    if (team == null) {
                        Clogger.w(TAG, "⚠️ No active team found")
                        // only show if UI is ready
                        if (::binds.isInitialized) {
                            Snackbar.make(
                                binds.root,
                                "Please create or join a team first",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Clogger.i(TAG, "✅ Active team: ${team.name} (${team.id})")

                        // wrap in try-catch so no crash if not implemented
                        runCatching { userRepo.hydrateForTeam(team.id) }
                        runCatching { eventRepo.hydrateForTeam(team.id) }
                        runCatching { attendanceRepo.hydrateForTeam(team.id) }

                        if (!syncScheduled) {
                            runCatching { SyncScheduler.schedule(this@HomeActivity) }
                            syncScheduled = true
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        val notificationOption = menu?.findItem(R.id.menu_home_notifications)
        val notificationAction = notificationOption?.actionView
        notificationBadge = notificationAction?.findViewById<ImageView>(R.id.iv_badge)
        notificationAction?.setOnClickListener {
            onOptionsItemSelected(notificationOption)
        }

        return true
    }

    // --- ViewModel
    private fun observe() = model.uiConfiguration.observe(this) { configuration ->
        when (configuration.appBarShown) {
            true -> {
                binds.topBar.visibility = View.VISIBLE
                binds.appBar.title = configuration.appBarTitle
                when (configuration.appBarColor) {
                    AppbarTheme.NIGHT -> {
                        binds.root.setBackgroundColor("#161620".toColorInt())
                        WindowCompat.getInsetsController(
                            window,
                            window.decorView
                        ).isAppearanceLightStatusBars = false
                        binds.topBar.setBackgroundColor("#161620".toColorInt())
                        binds.appBar.setTitleTextColor("#FFFFFF".toColorInt())
                    }
                    else -> {
                        binds.root.setBackgroundColor("#FFFFFF".toColorInt())
                        WindowCompat.getInsetsController(
                            window,
                            window.decorView
                        ).isAppearanceLightStatusBars = true
                        binds.topBar.setBackgroundColor("#FFFFFF".toColorInt())
                        binds.appBar.setTitleTextColor("#161620".toColorInt())
                    }
                }
            }
            else -> {
                binds.topBar.visibility = View.GONE
            }
        }
    }

    // --- Internals
    fun setNotificationBadgeVisibility(visible: Boolean) {
        if (visible) notificationBadge?.visibility = View.VISIBLE
        else notificationBadge?.visibility = View.GONE
    }

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
        val notificationsItem = binds.appBar.menu.findItem(R.id.menu_home_notifications)
        // Click via menu item listener (fallback if actionView not present)
        notificationsItem.setOnMenuItemClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
            true
        }

        // If using a custom actionLayout, clicks land on the actionView, not the MenuItem
        val actionView = notificationsItem.actionView
        actionView?.apply {
            contentDescription = getString(R.string.app_name)
            // Click anywhere on the action view
            setOnClickListener { openNotifications() }

            // Also wire inner clickable views from the custom layout
            findViewById<View?>(R.id.cv_menu_icon)?.setOnClickListener { openNotifications() }
            findViewById<View?>(R.id.iv_menu_icon)?.setOnClickListener { openNotifications() }
        }
    }

    private fun openNotifications() {
        val intent = Intent(this, NotificationsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode =
            BottomNavigationView.LABEL_VISIBILITY_UNLABELED // Ensure labels are always visible

        bottomNav.setOnItemSelectedListener { menuItem ->
            // Debounce rapid taps to prevent jitter/duplicate transactions
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastBottomNavClickAt < 200L) return@setOnItemSelectedListener false
            lastBottomNavClickAt = now

            val newTabIndex = when (menuItem.itemId) {
                R.id.nav_calendar -> 0
                R.id.nav_team_players -> 1
                R.id.nav_player_profile -> 2
                R.id.nav_profile -> 3
                else -> -1
            }

            if (newTabIndex != -1) {
                // Ignore re-select on the same tab to prevent jitter
                if (newTabIndex == currentTabIndex) return@setOnItemSelectedListener true

                val fragment = when (menuItem.itemId) {
                    R.id.nav_calendar -> HomeCalendarFragment()
                    R.id.nav_team_players -> HomeTeamFragment()
                    R.id.nav_player_profile -> profileFragmentForCurrentUser()
                    R.id.nav_profile -> HomeSettingsFragment()
                    else -> null
                }

                fragment?.let {
                    switchFragmentWithDirection(it, newTabIndex)
                }
                true
            } else {
                false
            }
        }

        // Do nothing on reselect to avoid spamming transitions
        bottomNav.setOnItemReselectedListener { /* no-op */ }

        // Setup long click listener for Options tab specifically
        setupOptionsLongClick(bottomNav)

        // Default fragment is set in onCreate
    }

    // Public helper to navigate to a specific bottom-nav tab programmatically
    fun navigateToTab(menuItemId: Int) {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        val newTabIndex = when (menuItemId) {
            R.id.nav_calendar -> 0
            R.id.nav_team_players -> 1
            R.id.nav_player_profile -> 2
            R.id.nav_profile -> 3
            else -> currentTabIndex
        }
        val fragment = when (menuItemId) {
            R.id.nav_calendar -> HomeCalendarFragment()
            R.id.nav_team_players -> HomeTeamFragment()
            R.id.nav_player_profile -> profileFragmentForCurrentUser()
            R.id.nav_profile -> HomeSettingsFragment()
            else -> null
        }
        fragment?.let {
            switchFragmentWithDirection(it, newTabIndex)
            bottomNav.selectedItemId = menuItemId
        }
    }

    private fun profileFragmentForCurrentUser(): Fragment {
        // Return the new AccountFragment for the current logged-in user
        return AccountFragment()
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
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Switched to ${selectedTeam.teamName}",
                    Snackbar.LENGTH_SHORT
                ).show()
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

        Snackbar.make(
            findViewById(android.R.id.content),
            "Default team updated",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun switchFragment(fragment: Fragment) {
        // Legacy method - use default transition
        switchFragmentWithDirection(fragment, currentTabIndex)
    }

    private fun switchFragmentWithDirection(fragment: Fragment, newTabIndex: Int) {
        // Prevent unnecessary fragment switching
        if (currentFragment?.javaClass == fragment.javaClass && currentTabIndex == newTabIndex) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()

        // Determine transition direction based on tab index
        val isMovingForward = newTabIndex > currentTabIndex
        val isMovingBackward = newTabIndex < currentTabIndex

        // Set appropriate animations based on direction
        if (isMovingForward) {
            // Moving forward (left to right in tab order) - slide in from right
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else if (isMovingBackward) {
            // Moving backward (right to left in tab order) - slide in from left
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        } else {
            // Same tab or first load - use fade transition
            transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        // Clear previous fragment reference to prevent memory leaks
        currentFragment?.let { 
            if (it.isAdded) {
                transaction.remove(it)
            }
        }

        // Replace without pushing to back stack to avoid jitter/stack growth for bottom nav
        transaction.setReorderingAllowed(true)
        transaction.replace(R.id.fragmentContainer, fragment)

        transaction.commit()
        currentFragment = fragment
        currentTabIndex = newTabIndex
    }

    // Modern back navigation using OnBackPressedDispatcher
    private val onBackPressedCallback = object : androidx.activity.OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Handle back navigation
            if (supportFragmentManager.backStackEntryCount > 1) {
                // Pop the back stack
                supportFragmentManager.popBackStack()

                // Update current tab index based on the fragment that's now visible
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                currentTabIndex = when (currentFragment) {
                    is HomeCalendarFragment -> 0
                    is HomeTeamFragment -> 1
                    is PlayerProfileFragment -> 2
                    is HomeSettingsFragment -> 3
                    else -> currentTabIndex
                }
            } else {
                // Exit the app if we're at the root
                finishAffinity()
            }
        }
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
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (currentFragment is ProfileFragment) {
                    // Refresh profile with new team data
                    currentFragment.onResume()
                }
            }
            .show(supportFragmentManager, "AccountSwitcher")
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}

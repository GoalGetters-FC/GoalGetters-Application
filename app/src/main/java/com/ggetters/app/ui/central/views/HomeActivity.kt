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
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityHomeBinding
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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


    var notificationBadge: ImageView? = null


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()

        setupStatusBar()
        setupViews()
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            switchFragment(HomeCalendarFragment())
        }

        observe()

        model.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.WHITE,
                appBarTitle = "August 2025",
                appBarShown = true,
            )
        )
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        val notificationOption = menu?.findItem(R.id.menu_home_notifications)
        val notificationAction = notificationOption?.actionView
        val notificationBadge = notificationAction?.findViewById<ImageView>(R.id.iv_badge)
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
        binds.appBar.menu.findItem(R.id.menu_home_notifications).setOnMenuItemClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            true
        }
    }


    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode =
            BottomNavigationView.LABEL_VISIBILITY_UNLABELED // Ensure labels are always visible

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_calendar -> {
                    switchFragment(HomeCalendarFragment())
                    true
                }

                R.id.nav_team_players -> { // Corrected ID
                    switchFragment(HomeTeamFragment())
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
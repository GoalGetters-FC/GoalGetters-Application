package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.NotificationCardAdapter
import com.ggetters.app.data.model.Notification
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.ggetters.app.core.services.ComprehensiveNotificationTestService
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val model: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationCardAdapter
    
    @Inject
    lateinit var comprehensiveTestService: ComprehensiveNotificationTestService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Enable smooth activity transitions
        supportPostponeEnterTransition()
        supportStartPostponedEnterTransition()

        setupWindowInsets()
        setupHeader()
        setupNotifications()
        setupTestButton()
        
        // Load notifications when activity starts
        model.loadNotifications()
    }

    private fun setupWindowInsets() {
        // Enable edge-to-edge display but keep status bar visible
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set up window insets controller for light status bar
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true // Light status bar icons

        // Ensure status bar is visible and properly colored
        window.statusBarColor = getColor(R.color.white)
        
        // Handle window insets for the header
        val headerLayout = findViewById<android.widget.LinearLayout>(R.id.headerLayout)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, 0)
            windowInsets
        }
    }

    private fun setupHeader() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create new card-based adapter
        notificationAdapter = NotificationCardAdapter(
            onNotificationClick = { notification ->
                // Handle notification click - could navigate to event details
                // TODO: Navigate to event details
            },
            onMarkAsSeen = { notification ->
                // Mark as seen
                lifecycleScope.launch {
                    model.markAsSeen(notification.id)
                }
            },
            onDelete = { notification ->
                // Delete notification
                lifecycleScope.launch {
                    model.deleteNotification(notification.id)
                }
            }
        )
        recyclerView.adapter = notificationAdapter

        // Observe notifications from ViewModel
        lifecycleScope.launch {
                model.notifications.collect { notifications ->
                // Update adapter with new notifications
                notificationAdapter.updateNotifications(notifications)
            }
        }

        // Observe loading state
        lifecycleScope.launch {
                model.isLoading.collect { isLoading ->
                // TODO: Show/hide loading indicator
            }
        }

        // Observe error state
        lifecycleScope.launch {
                model.error.collect { error ->
                    error?.let {
                        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show()
                        model.clearError()
                    }
                }
            }
        }
        
    private fun setupTestButton() {
        val testButton = findViewById<ImageButton>(R.id.testButton)
        testButton.setOnClickListener {
            // Create sample notifications for testing
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Get user's team ID from the model
                lifecycleScope.launch {
                    try {
                        val teamId = model.getCurrentTeamId() ?: "test-team-id"
                        comprehensiveTestService.runComprehensiveNotificationTest(currentUser.uid, teamId)
                        Snackbar.make(findViewById(android.R.id.content), "Test notifications created!", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to create test notifications: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Please log in to test notifications", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
} 
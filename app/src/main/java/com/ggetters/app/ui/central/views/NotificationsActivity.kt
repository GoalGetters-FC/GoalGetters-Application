package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.NotificationAdapter
import com.ggetters.app.data.model.NotificationItem
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.model.LinkedEventType
import com.ggetters.app.data.model.AttendanceCounts
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import android.widget.ImageButton

// TODO: Backend - Implement real-time notification delivery using WebSocket or FCM
// TODO: Backend - Add notification preferences and user settings
// TODO: Backend - Implement notification analytics and engagement tracking
// TODO: Backend - Add notification templates and automated notifications
// TODO: Backend - Implement notification scheduling and delayed delivery
// TODO: Backend - Add notification grouping and smart categorization
// TODO: Backend - Implement notification search and filtering
// TODO: Backend - Add notification export and backup functionality

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val model: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private var allNotifications: List<NotificationItem> = emptyList()
    private var filteredNotifications: List<NotificationItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Enable smooth activity transitions
        supportPostponeEnterTransition()
        supportStartPostponedEnterTransition()

        requestNotificationPermissionIfNeeded()
        setupWindowInsets()
        setupHeader()
        setupNotifications()
        setupSwipeActions()
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
        ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top + view.paddingTop,
                view.paddingRight,
                view.paddingBottom
            )
            windowInsets
        }
    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Subscribe to topics for current user/team if available
        // TODO: Replace null with active teamId from your app state
        model.subscribeTopicsForCurrentUser(activeTeamId = null)

        // Observe notifications from ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.notifications.collect { notifications ->
                    allNotifications = notifications.map { notification ->
                        // Convert Notification to NotificationItem for compatibility
                        NotificationItem(
                            id = notification.id.hashCode(),
                            sourceId = notification.id,
                            title = notification.title,
                            subtitle = notification.subtitle,
                            message = notification.message,
                            isSeen = notification.isSeen,
                            type = notification.type,
                            timestamp = notification.createdAt.toEpochMilli(),
                            sender = notification.sender,
                            linkedEventType = notification.linkedEventType,
                            linkedEventId = notification.linkedEventId,
                            // Attempt to parse notification.data JSON string to a Map for UI cards
                            data = try { 
                                val json = org.json.JSONObject(notification.data)
                                json.keys().asSequence().associateWith { key -> json.get(key) }
                            } catch (e: Exception) { emptyMap<String, Any>() }
                        )
                    }
                    filteredNotifications = allNotifications
                    notificationAdapter.updateNotifications(filteredNotifications.toMutableList())
                }
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.isLoading.collect { isLoading ->
                    // Show/hide loading indicator
                    if (isLoading) {
                        // Show loading
                    } else {
                        // Hide loading
                    }
                }
            }
        }

        // Observe error state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.error.collect { error ->
                    error?.let {
                        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show()
                        model.clearError()
                    }
                }
            }
        }
        
        notificationAdapter = NotificationAdapter(
            filteredNotifications.toMutableList(),
            onActionClick = { notification, action ->
                when (action) {
                    "delete" -> {
                        showDeleteConfirmation(notification)
                    }
                    "mark_seen" -> {
                        handleMarkAsSeen(notification)
                    }
                    "pin" -> {
                        handlePinNotification(notification)
                    }
                    "view_event" -> {
                        handleViewLinkedEvent(notification)
                    }
                }
            },
            onItemClick = { notification ->
                // Handle notification item click - opens linked event
                handleNotificationClick(notification)
            },
            onSwipeAction = { notification, action ->
                when (action) {
                    "delete" -> showDeleteConfirmation(notification)
                    "mark_seen" -> handleMarkAsSeen(notification)
                }
            },
            onLongPress = { notification ->
                showLongPressOptions(notification)
            }
        )
        
        recyclerView.adapter = notificationAdapter
    }

    private fun setupSwipeActions() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        
        // TODO: Backend - Implement swipe actions for mark read/unread and delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = filteredNotifications[position]
                
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Swipe left - Mark as read/unread
                        handleMarkAsSeen(notification)
                    }
                    ItemTouchHelper.RIGHT -> {
                        // Swipe right - Delete
                        showDeleteConfirmation(notification)
                    }
                }
            }
        }
        
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        // TODO: Backend - Navigate to linked event based on notification.linkedEventType and notification.linkedEventId
        when (notification.linkedEventType) {
            LinkedEventType.GAME -> {
                // Navigate to game details
                Snackbar.make(findViewById(android.R.id.content), "Opening game details...", Snackbar.LENGTH_SHORT).show()
            }
            LinkedEventType.PRACTICE -> {
                // Navigate to practice details
                Snackbar.make(findViewById(android.R.id.content), "Opening practice details...", Snackbar.LENGTH_SHORT).show()
            }
            LinkedEventType.ANNOUNCEMENT -> {
                // Navigate to announcement details
                Snackbar.make(findViewById(android.R.id.content), "Opening announcement details...", Snackbar.LENGTH_SHORT).show()
            }
            LinkedEventType.MATCH_RESULTS -> {
                // Navigate to match results
                Snackbar.make(findViewById(android.R.id.content), "Opening match results...", Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                // Default behavior
                if (!notification.isSeen) {
                    handleMarkAsSeen(notification)
                }
            }
        }
    }



    private fun handleMarkAsSeen(notification: NotificationItem) {
        model.markAsSeen(notification.sourceId, !notification.isSeen)
        
        val action = if (notification.isSeen) "marked as read" else "marked as unread"
        Snackbar.make(
            findViewById(android.R.id.content), 
            "Notification $action", 
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handlePinNotification(notification: NotificationItem) {
        model.pinNotification(notification.sourceId, !notification.isPinned)
        
        val action = if (notification.isPinned) "pinned" else "unpinned"
        Snackbar.make(
            findViewById(android.R.id.content), 
            "Notification $action", 
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handleViewLinkedEvent(notification: NotificationItem) {
        // Navigate to linked event
        handleNotificationClick(notification)
    }

    private fun showLongPressOptions(notification: NotificationItem) {
        // TODO: Backend - Show bottom sheet with long press options
        val options = arrayOf(
            if (notification.isSeen) "Mark as Unread" else "Mark as Read",
            if (notification.isPinned) "Unpin" else "Pin",
            "View Linked Event",
            "Delete"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Notification Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handleMarkAsSeen(notification)
                    1 -> handlePinNotification(notification)
                    2 -> handleViewLinkedEvent(notification)
                    3 -> showDeleteConfirmation(notification)
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(notification: NotificationItem) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                model.deleteNotification(notification.sourceId)
                Snackbar.make(findViewById(android.R.id.content), "Notification deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
} 
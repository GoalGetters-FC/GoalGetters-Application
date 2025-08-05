package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.NotificationAdapter
import com.ggetters.app.ui.central.models.NotificationItem
import com.ggetters.app.ui.central.models.NotificationType
import com.ggetters.app.ui.central.models.RSVPStatus
import com.ggetters.app.ui.central.models.AttendanceCounts
import com.ggetters.app.ui.central.models.LinkedEventType
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
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

        setupHeader()
        setupNotifications()
        setupSwipeActions()
    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // TODO: Backend - Fetch notifications from backend with proper data structure
        // TODO: Backend - Implement notification pagination for large datasets
        // TODO: Backend - Add notification caching for offline access
        // TODO: Backend - Implement notification sync across devices
        val notifications = listOf(
            // General Text Notification (matches image)
            NotificationItem(
                id = 1,
                title = "Reminder",
                subtitle = "Shin guards reminder",
                message = "Don't forget to pack your shin guards for tomorrow!",
                isSeen = false,
                type = NotificationType.ADMIN_MESSAGE,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "Coach",
                linkedEventType = LinkedEventType.ANNOUNCEMENT,
                linkedEventId = "announcement_001"
            ),
            
            // Results Summary Notification (matches image)
            NotificationItem(
                id = 2,
                title = "Match Results",
                subtitle = "Summary of results",
                message = "Summary of results.",
                isSeen = false,
                type = NotificationType.POST_MATCH_SUMMARY,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "System",
                linkedEventType = LinkedEventType.MATCH_RESULTS,
                linkedEventId = "match_001",
                data = mapOf(
                    "homeScore" to "15",
                    "awayScore" to "2",
                    "homeTeam" to "Home",
                    "awayTeam" to "Away"
                )
            ),
            
            // Scheduled Event Notification (matches image)
            NotificationItem(
                id = 3,
                title = "New Practice",
                subtitle = "Practice scheduled",
                message = "New practice scheduled!",
                isSeen = false,
                type = NotificationType.PRACTICE_RSVP,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "Coach",
                linkedEventType = LinkedEventType.PRACTICE,
                linkedEventId = "practice_001",
                eventDate = System.currentTimeMillis() + 86400000, // Tomorrow
                attendanceCounts = AttendanceCounts(8, 3, 2, 13)
            ),
            
            // Long Text Notification (matches image)
            NotificationItem(
                id = 4,
                title = "Parent Pickup",
                subtitle = "Important notice",
                message = "Parents: remember to pick up your players from the front not the back. Thanks.",
                isSeen = true,
                type = NotificationType.ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() + 86400000, // Future date
                sender = "Coach",
                linkedEventType = LinkedEventType.ANNOUNCEMENT,
                linkedEventId = "announcement_002"
            ),
            
            // Game RSVP Notification with attendance counts
            NotificationItem(
                id = 5,
                title = "Match vs Tigers FC",
                subtitle = "RSVP Request",
                message = "Please confirm your availability for the upcoming match.",
                isSeen = false,
                type = NotificationType.GAME_RSVP,
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                sender = "Coach",
                linkedEventType = LinkedEventType.GAME,
                linkedEventId = "game_001",
                venue = "Main Stadium",
                eventDate = System.currentTimeMillis() + 86400000, // Tomorrow
                opponent = "Tigers FC",
                rsvpStatus = RSVPStatus.NOT_RESPONDED,
                attendanceCounts = AttendanceCounts(12, 2, 1, 15)
            ),
            
            // Game Reminder Notification
            NotificationItem(
                id = 6,
                title = "Reminder: Match today",
                subtitle = "Home match against Greenfield United",
                message = "Your team has a match this weekend at the home ground. Please arrive 30 minutes early.",
                isSeen = true,
                type = NotificationType.GAME_REMINDER,
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                sender = "System",
                linkedEventType = LinkedEventType.GAME,
                linkedEventId = "game_002",
                venue = "Home Ground",
                eventDate = System.currentTimeMillis() + 7200000, // 2 hours from now
                opponent = "Greenfield United",
                countdownTime = System.currentTimeMillis() + 7200000
            ),
            
            // Practice RSVP Notification
            NotificationItem(
                id = 7,
                title = "Training Session Tomorrow",
                subtitle = "RSVP Request",
                message = "Team practice scheduled for tomorrow at 3 PM. Focus on passing drills.",
                isSeen = false,
                type = NotificationType.PRACTICE_RSVP,
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                sender = "Coach",
                linkedEventType = LinkedEventType.PRACTICE,
                linkedEventId = "practice_002",
                venue = "Field 2",
                eventDate = System.currentTimeMillis() + 86400000, // Tomorrow
                rsvpStatus = RSVPStatus.AVAILABLE,
                attendanceCounts = AttendanceCounts(10, 3, 2, 15)
            ),
            
            // Pinned Announcement
            NotificationItem(
                id = 8,
                title = "Team Announcement",
                subtitle = "Important team meeting",
                message = "Team meeting this Friday at 6 PM to discuss upcoming tournament strategy.",
                isSeen = false,
                type = NotificationType.ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                sender = "Coach",
                isPinned = true,
                linkedEventType = LinkedEventType.ANNOUNCEMENT,
                linkedEventId = "announcement_003"
            )
        )

        allNotifications = notifications.sortedByDescending { it.timestamp } // Newest first
        filteredNotifications = allNotifications
        
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
            onRSVPClick = { notification, status ->
                handleRSVPResponse(notification, status)
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

    private fun handleRSVPResponse(notification: NotificationItem, status: RSVPStatus) {
        // TODO: Backend - Send RSVP response to backend and update attendance counts
        // TODO: Backend - Implement real-time attendance updates for coaches
        // TODO: Backend - Add RSVP analytics and response tracking
        // TODO: Backend - Implement automated reminders for pending RSVPs
        notification.rsvpStatus = status
        notificationAdapter.notifyDataSetChanged()
        
        val statusText = when (status) {
            RSVPStatus.AVAILABLE -> "Available"
            RSVPStatus.MAYBE -> "Maybe"
            RSVPStatus.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
        
        Snackbar.make(
            findViewById(android.R.id.content), 
            "RSVP status set to: $statusText", 
            Snackbar.LENGTH_SHORT
        ).show()
        
        // TODO: Backend - Update calendar event status and attendance counts
        // TODO: Backend - Notify coaches of RSVP changes for lineup adjustments
    }

    private fun handleMarkAsSeen(notification: NotificationItem) {
        // TODO: Backend - Mark notification as seen/unseen in backend
        // TODO: Backend - Implement notification read status synchronization
        // TODO: Backend - Add notification analytics for engagement tracking
        // TODO: Backend - Implement bulk read/unread operations
        notification.isSeen = !notification.isSeen
        notificationAdapter.notifyDataSetChanged()
        
        val action = if (notification.isSeen) "marked as read" else "marked as unread"
        Snackbar.make(
            findViewById(android.R.id.content), 
            "Notification $action", 
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handlePinNotification(notification: NotificationItem) {
        // TODO: Backend - Pin/unpin notification in backend
        // TODO: Backend - Implement pinned notification persistence
        // TODO: Backend - Add pinned notification limits and management
        // TODO: Backend - Implement pinned notification sync across devices
        notification.isPinned = !notification.isPinned
        notificationAdapter.notifyDataSetChanged()
        
        val action = if (notification.isPinned) "pinned" else "unpinned"
        Snackbar.make(
            findViewById(android.R.id.content), 
            "Notification $action", 
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handleViewLinkedEvent(notification: NotificationItem) {
        // TODO: Backend - Navigate to linked event
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
                // TODO: Backend - Delete notification from backend
                // TODO: Backend - Implement soft delete for notification recovery
                // TODO: Backend - Add notification deletion analytics
                // TODO: Backend - Implement bulk delete operations
                allNotifications = allNotifications.filter { it.id != notification.id }
                filteredNotifications = filteredNotifications.filter { it.id != notification.id }
                notificationAdapter.updateNotifications(filteredNotifications.toMutableList())
                Snackbar.make(findViewById(android.R.id.content), "Notification deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
} 
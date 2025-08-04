package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.NotificationAdapter
import com.ggetters.app.ui.central.models.NotificationItem
import com.ggetters.app.ui.central.models.NotificationType
import com.ggetters.app.ui.central.models.RSVPStatus
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import android.widget.ImageButton

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val model: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var filterChipGroup: ChipGroup
    private var allNotifications: List<NotificationItem> = emptyList()
    private var filteredNotifications: List<NotificationItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        setupHeader()
        setupNotifications()
    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample notifications data with enhanced structure
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
                sender = "Coach"
            ),
            
            // Results Summary Notification (matches image)
            NotificationItem(
                id = 2,
                title = "Match Results",
                subtitle = "Summary of results",
                message = "Summary of results.",
                isSeen = false,
                type = NotificationType.ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "System",
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
                eventDate = System.currentTimeMillis() + 86400000 // Tomorrow
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
                sender = "Coach"
            ),
            
            // Game RSVP Notification
            NotificationItem(
                id = 5,
                title = "Match vs Tigers FC",
                subtitle = "RSVP Request",
                message = "Please confirm your availability for the upcoming match.",
                isSeen = false,
                type = NotificationType.GAME_RSVP,
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                sender = "Coach",
                eventId = "game_001",
                venue = "Main Stadium",
                eventDate = System.currentTimeMillis() + 86400000, // Tomorrow
                opponent = "Tigers FC",
                rsvpStatus = RSVPStatus.NOT_RESPONDED
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
                eventId = "game_002",
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
                eventId = "practice_001",
                venue = "Field 2",
                eventDate = System.currentTimeMillis() + 86400000, // Tomorrow
                rsvpStatus = RSVPStatus.AVAILABLE
            ),
            
            // Practice Reminder Notification
            NotificationItem(
                id = 8,
                title = "Practice Reminder",
                subtitle = "Training at 3 PM - Field 2",
                message = "Don't forget to bring your training gear and water bottle.",
                isSeen = true,
                type = NotificationType.PRACTICE_REMINDER,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "Coach",
                eventId = "practice_002",
                venue = "Field 2",
                eventDate = System.currentTimeMillis() + 3600000, // 1 hour from now
                countdownTime = System.currentTimeMillis() + 3600000
            ),
            
            // Announcement Notification
            NotificationItem(
                id = 9,
                title = "Team Announcement",
                subtitle = "Important team meeting",
                message = "Team meeting this Friday at 6 PM to discuss upcoming tournament strategy.",
                isSeen = false,
                type = NotificationType.ANNOUNCEMENT,
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                sender = "Coach",
                isPinned = true
            ),
            
            // Player Update Notification
            NotificationItem(
                id = 10,
                title = "New Player Joined",
                subtitle = "John Doe added to roster",
                message = "John Doe has joined your team and will be available for the next match.",
                isSeen = true,
                type = NotificationType.PLAYER_UPDATE,
                timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
                sender = "System"
            ),
            
            // Schedule Change Notification
            NotificationItem(
                id = 11,
                title = "Schedule Change",
                subtitle = "Practice time updated",
                message = "Tomorrow's practice has been moved from 3 PM to 4 PM due to field maintenance.",
                isSeen = false,
                type = NotificationType.SCHEDULE_CHANGE,
                timestamp = System.currentTimeMillis() - 1800000, // 30 minutes ago
                sender = "Coach"
            ),
            
            // Admin Message Notification
            NotificationItem(
                id = 12,
                title = "Message from Coach",
                subtitle = "Don't forget shin guards",
                message = "Don't forget to bring your shin guards for tomorrow's game. They're mandatory for safety.",
                isSeen = true,
                type = NotificationType.ADMIN_MESSAGE,
                timestamp = System.currentTimeMillis() - 43200000, // 12 hours ago
                sender = "Coach John"
            )
        )

        allNotifications = notifications
        filteredNotifications = notifications
        
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
                }
            },
            onItemClick = { notification ->
                // Handle notification item click
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
            }
        )
        
        recyclerView.adapter = notificationAdapter
    }

    private fun showDeleteConfirmation(notification: NotificationItem) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                // TODO: Backend - Delete notification from backend
                allNotifications = allNotifications.filter { it.id != notification.id }
                filteredNotifications = filteredNotifications.filter { it.id != notification.id }
                notificationAdapter.updateNotifications(filteredNotifications.toMutableList())
                Snackbar.make(findViewById(android.R.id.content), "Notification deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        // Simple click handling - could show details or mark as read
        if (!notification.isSeen) {
            handleMarkAsSeen(notification)
        }
    }

    private fun handleRSVPResponse(notification: NotificationItem, status: RSVPStatus) {
        // TODO: Backend - Send RSVP response to backend
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
    }

    private fun handleMarkAsSeen(notification: NotificationItem) {
        // TODO: Backend - Mark notification as seen/unseen
        notification.isSeen = !notification.isSeen
        notificationAdapter.notifyDataSetChanged()
        
        val action = if (notification.isSeen) "marked as read" else "marked as unread"
        Snackbar.make(
            findViewById(android.R.id.content), 
            "Notification $action", 
            Snackbar.LENGTH_SHORT
        ).show()
    }
} 
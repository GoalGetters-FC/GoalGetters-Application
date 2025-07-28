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
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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

        setupToolbar()
        setupFilterChips()
        setupNotifications()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Notifications"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupFilterChips() {
        filterChipGroup = findViewById(R.id.filterChipGroup)
        filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            val filterType = when (selectedChip?.id) {
                R.id.chipAll -> "all"
                R.id.chipGames -> "game"
                R.id.chipPractices -> "practice"
                R.id.chipAnnouncements -> "announcement"
                else -> "all"
            }
            filterNotifications(filterType)
        }
    }

    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample notifications data with enhanced structure
        val notifications = listOf(
            NotificationItem(
                id = 1,
                title = "Game Reminder: Saturday 15:00",
                subtitle = "Home match against Greenfield United",
                message = "Your team has a match this weekend at the home ground. Please arrive 30 minutes early.",
                isSeen = false,
                type = "game",
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                sender = "Coach"
            ),
            NotificationItem(
                id = 2,
                title = "Practice Session Tomorrow",
                subtitle = "Training at 3 PM - Field 2",
                message = "Team practice scheduled for tomorrow at 3 PM. Focus on passing drills.",
                isSeen = true,
                type = "practice",
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                sender = "Coach"
            ),
            NotificationItem(
                id = 3,
                title = "New Player Joined",
                subtitle = "John Doe added to roster",
                message = "John Doe has joined your team and will be available for the next match.",
                isSeen = true,
                type = "player",
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                sender = "System"
            ),
            NotificationItem(
                id = 4,
                title = "Team Announcement",
                subtitle = "Important team meeting",
                message = "Team meeting this Friday at 6 PM to discuss upcoming tournament strategy.",
                isSeen = false,
                type = "announcement",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                sender = "Coach"
            ),
            NotificationItem(
                id = 5,
                title = "Role Updated",
                subtitle = "You are now Assistant Coach",
                message = "Your role has been updated to Assistant Coach. You can now manage practice schedules.",
                isSeen = true,
                type = "admin",
                timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
                sender = "System"
            )
        )

        allNotifications = notifications
        filteredNotifications = notifications
        
        notificationAdapter = NotificationAdapter(
            filteredNotifications.toMutableList(),
            onActionClick = { notification, action ->
                when (action) {
                    "delete" -> {
                        // TODO: Backend - Delete notification
                        showDeleteConfirmation(notification)
                    }
                    "mark_seen" -> {
                        // TODO: Backend - Mark notification as seen
                        notification.isSeen = !notification.isSeen
                        notificationAdapter.notifyDataSetChanged()
                    }
                }
            },
            onItemClick = { notification ->
                // TODO: Backend - Open notification details
                showNotificationDetails(notification)
            }
        )

        recyclerView.adapter = notificationAdapter
    }

    private fun filterNotifications(filterType: String) {
        filteredNotifications = if (filterType == "all") {
            allNotifications
        } else {
            allNotifications.filter { it.type == filterType }
        }
        
        notificationAdapter.updateNotifications(filteredNotifications.toMutableList())
    }

    private fun showDeleteConfirmation(notification: NotificationItem) {
        val dialog = AlertDialog.Builder(this, R.style.Theme_GoalGetters_Dialog)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                // TODO: Backend - Delete notification from backend
                // For now, just show a snackbar
                Snackbar.make(findViewById(android.R.id.content), "Notification deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.error, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        
        dialog.show()
    }

    private fun showNotificationDetails(notification: NotificationItem) {
        val dialog = AlertDialog.Builder(this, R.style.Theme_GoalGetters_Dialog)
            .setTitle(notification.title)
            .setMessage(notification.message)
            .setPositiveButton("Mark as ${if (notification.isSeen) "Unread" else "Read"}") { _, _ ->
                notification.isSeen = !notification.isSeen
                notificationAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Close", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.primary, null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notifications_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mark_all_read -> {
                markAllAsRead()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun markAllAsRead() {
        // TODO: Backend - Mark all notifications as read
        allNotifications.forEach { it.isSeen = true }
        filteredNotifications.forEach { it.isSeen = true }
        notificationAdapter.notifyDataSetChanged()
        Snackbar.make(findViewById(android.R.id.content), "All notifications marked as read", Snackbar.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 
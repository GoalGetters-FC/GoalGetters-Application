package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.NotificationItem
import com.ggetters.app.ui.central.models.NotificationType
import com.ggetters.app.ui.central.models.RSVPStatus
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onActionClick: (NotificationItem, String) -> Unit,
    private val onItemClick: (NotificationItem) -> Unit,
    private val onRSVPClick: (NotificationItem, RSVPStatus) -> Unit,
    private val onSwipeAction: (NotificationItem, String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val notificationText: TextView = itemView.findViewById(R.id.notificationText)
        private val notificationTimestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
        private val resultsSummary: LinearLayout = itemView.findViewById(R.id.resultsSummary)
        private val homeScore: TextView = itemView.findViewById(R.id.homeScore)
        private val homeLabel: TextView = itemView.findViewById(R.id.homeLabel)
        private val awayScore: TextView = itemView.findViewById(R.id.awayScore)
        private val awayLabel: TextView = itemView.findViewById(R.id.awayLabel)
        private val scheduledEvent: LinearLayout = itemView.findViewById(R.id.scheduledEvent)
        private val eventIcon: ImageView = itemView.findViewById(R.id.eventIcon)
        private val eventDateTime: TextView = itemView.findViewById(R.id.eventDateTime)
        private val actionMenuButton: ImageButton = itemView.findViewById(R.id.actionMenuButton)

        fun bind(notification: NotificationItem) {
            // Set main notification text
            notificationText.text = notification.message
            
            // Set timestamp
            notificationTimestamp.text = formatTimestamp(notification.timestamp)
            
            // Set icon based on notification type
            setNotificationIcon(notification.type)
            
            // Set visual states
            setVisualStates(notification)
            
            // Set up specific notification types
            setupNotificationType(notification)
            
            // Set up click listeners
            setupClickListeners(notification)
        }

        private fun setNotificationIcon(type: NotificationType) {
            val iconRes = when (type) {
                NotificationType.GAME_RSVP, NotificationType.GAME_REMINDER -> R.drawable.ic_unicons_calender_24
                NotificationType.PRACTICE_RSVP, NotificationType.PRACTICE_REMINDER -> R.drawable.ic_unicons_clock_24
                NotificationType.ANNOUNCEMENT -> R.drawable.ic_unicons_bell_24
                NotificationType.PLAYER_UPDATE -> R.drawable.ic_unicons_user_24
                NotificationType.ADMIN_MESSAGE -> R.drawable.ic_unicons_message_24
                NotificationType.SCHEDULE_CHANGE -> R.drawable.ic_unicons_settings_24
                NotificationType.SYSTEM -> R.drawable.ic_unicons_bell_24
            }
            notificationIcon.setImageResource(iconRes)
        }

        private fun setVisualStates(notification: NotificationItem) {
            // Set unread indicator
            unreadIndicator.visibility = if (!notification.isSeen) View.VISIBLE else View.GONE
            
            // Set text style based on read status
            if (notification.isSeen) {
                notificationText.setTextColor(itemView.context.getColor(R.color.text_secondary))
                notificationText.setTypeface(null, android.graphics.Typeface.NORMAL)
            } else {
                notificationText.setTextColor(itemView.context.getColor(R.color.black))
                notificationText.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        private fun setupNotificationType(notification: NotificationItem) {
            // Hide all special layouts first
            resultsSummary.visibility = View.GONE
            scheduledEvent.visibility = View.GONE
            
            when (notification.type) {
                NotificationType.GAME_RSVP, NotificationType.PRACTICE_RSVP -> {
                    // Show scheduled event for RSVP notifications
                    scheduledEvent.visibility = View.VISIBLE
                    notification.eventDate?.let { date ->
                        eventDateTime.text = formatEventDateTime(date)
                    }
                }
                NotificationType.GAME_REMINDER, NotificationType.PRACTICE_REMINDER -> {
                    // Show scheduled event for reminder notifications
                    scheduledEvent.visibility = View.VISIBLE
                    notification.eventDate?.let { date ->
                        eventDateTime.text = formatEventDateTime(date)
                    }
                }
                NotificationType.ANNOUNCEMENT -> {
                    // Check if it's a results announcement
                    if (notification.message.contains("Summary of results") || 
                        notification.message.contains("Match result")) {
                        resultsSummary.visibility = View.VISIBLE
                        setupResultsSummary(notification)
                    }
                }
                else -> {
                    // Regular text notification - no special layout needed
                }
            }
        }

        private fun setupResultsSummary(notification: NotificationItem) {
            // Extract scores from notification data or use defaults
            val homeScoreValue = notification.data["homeScore"] as? String ?: "15"
            val awayScoreValue = notification.data["awayScore"] as? String ?: "2"
            val homeTeamName = notification.data["homeTeam"] as? String ?: "Home"
            val awayTeamName = notification.data["awayTeam"] as? String ?: "Away"
            
            homeScore.text = homeScoreValue
            awayScore.text = awayScoreValue
            homeLabel.text = homeTeamName
            awayLabel.text = awayTeamName
            
            // Set background colors based on win/loss
            val isWin = homeScoreValue.toIntOrNull() ?: 0 > awayScoreValue.toIntOrNull() ?: 0
            if (isWin) {
                resultsSummary.setBackgroundResource(R.drawable.result_home_background)
            } else {
                resultsSummary.setBackgroundResource(R.drawable.result_away_background)
            }
        }

        private fun setupClickListeners(notification: NotificationItem) {
            // Main item click
            itemView.setOnClickListener {
                onItemClick(notification)
            }
            
            // Action menu button
            actionMenuButton.setOnClickListener {
                showActionMenu(notification)
            }
        }

        private fun showActionMenu(notification: NotificationItem) {
            // Create and show popup menu
            val popup = android.widget.PopupMenu(itemView.context, actionMenuButton)
            popup.menuInflater.inflate(R.menu.menu_notification_actions, popup.menu)
            
            // Set up menu item click listeners
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_mark_seen -> {
                        onActionClick(notification, "mark_seen")
                        true
                    }
                    R.id.action_delete -> {
                        onActionClick(notification, "delete")
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                diff < 604800000 -> "${diff / 86400000}d ago"
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }

        private fun formatEventDateTime(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            return formatter.format(date)
        }
    }
} 
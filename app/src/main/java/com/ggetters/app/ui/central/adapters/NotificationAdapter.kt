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
import com.google.android.material.button.MaterialButton
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
        private val pinIcon: ImageView = itemView.findViewById(R.id.pinIcon)
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        private val notificationSubtitle: TextView = itemView.findViewById(R.id.notificationSubtitle)
        private val notificationTimestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
        private val eventDetails: LinearLayout = itemView.findViewById(R.id.eventDetails)
        private val eventDateTime: TextView = itemView.findViewById(R.id.eventDateTime)
        private val eventVenue: TextView = itemView.findViewById(R.id.eventVenue)
        private val countdownText: TextView = itemView.findViewById(R.id.countdownText)
        private val rsvpButtons: LinearLayout = itemView.findViewById(R.id.rsvpButtons)
        private val btnAvailable: MaterialButton = itemView.findViewById(R.id.btnAvailable)
        private val btnMaybe: MaterialButton = itemView.findViewById(R.id.btnMaybe)
        private val btnUnavailable: MaterialButton = itemView.findViewById(R.id.btnUnavailable)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val markSeenButton: ImageButton = itemView.findViewById(R.id.markSeenButton)

        fun bind(notification: NotificationItem) {
            // Set notification content
            notificationTitle.text = notification.title
            notificationSubtitle.text = notification.subtitle
            notificationTimestamp.text = formatTimestamp(notification.timestamp)
            
            // Set icon based on notification type
            setNotificationIcon(notification.type)
            
            // Set visual states
            setVisualStates(notification)
            
            // Set up event details for RSVP and reminder notifications
            setupEventDetails(notification)
            
            // Set up RSVP buttons
            setupRSVPButtons(notification)
            
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
            
            // Set pin icon
            pinIcon.visibility = if (notification.isPinned) View.VISIBLE else View.GONE
            
            // Set title style based on read status
            if (notification.isSeen) {
                itemView.setBackgroundResource(R.color.white)
                notificationTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
            } else {
                itemView.setBackgroundResource(R.color.surface_elevated)
                notificationTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        private fun setupEventDetails(notification: NotificationItem) {
            if (notification.type == NotificationType.GAME_RSVP || notification.type == NotificationType.PRACTICE_RSVP || 
                notification.type == NotificationType.GAME_REMINDER || notification.type == NotificationType.PRACTICE_REMINDER) {
                eventDetails.visibility = View.VISIBLE
                
                // Set date and time
                notification.eventDate?.let { date ->
                    eventDateTime.text = formatEventDateTime(date)
                }
                
                // Set venue/opponent
                when {
                    notification.opponent != null -> {
                        eventVenue.text = "vs ${notification.opponent}"
                    }
                    notification.venue != null -> {
                        eventVenue.text = notification.venue
                    }
                }
                
                // Set countdown for reminder notifications
                if (notification.type == NotificationType.GAME_REMINDER || notification.type == NotificationType.PRACTICE_REMINDER) {
                    countdownText.visibility = View.VISIBLE
                    notification.countdownTime?.let { countdown ->
                        countdownText.text = formatCountdown(countdown)
                    }
                } else {
                    countdownText.visibility = View.GONE
                }
            } else {
                eventDetails.visibility = View.GONE
            }
        }

        private fun setupRSVPButtons(notification: NotificationItem) {
            if (notification.type == NotificationType.GAME_RSVP || notification.type == NotificationType.PRACTICE_RSVP) {
                rsvpButtons.visibility = View.VISIBLE
                
                // Set current RSVP status
                updateRSVPButtonStates(notification)
                
                // Set click listeners for RSVP buttons
                btnAvailable.setOnClickListener {
                    onRSVPClick(notification, RSVPStatus.AVAILABLE)
                }
                
                btnMaybe.setOnClickListener {
                    onRSVPClick(notification, RSVPStatus.MAYBE)
                }
                
                btnUnavailable.setOnClickListener {
                    onRSVPClick(notification, RSVPStatus.UNAVAILABLE)
                }
            } else {
                rsvpButtons.visibility = View.GONE
            }
        }

        private fun updateRSVPButtonStates(notification: NotificationItem) {
            // Reset all buttons to default state
            btnAvailable.setBackgroundTintList(null)
            btnMaybe.setBackgroundTintList(null)
            btnUnavailable.setBackgroundTintList(null)
            
            // Highlight the selected RSVP status
            when (notification.rsvpStatus) {
                RSVPStatus.AVAILABLE -> {
                    btnAvailable.setBackgroundTintList(itemView.context.getColorStateList(R.color.success))
                    btnAvailable.setTextColor(itemView.context.getColor(R.color.white))
                }
                RSVPStatus.MAYBE -> {
                    btnMaybe.setBackgroundTintList(itemView.context.getColorStateList(R.color.warning))
                    btnMaybe.setTextColor(itemView.context.getColor(R.color.white))
                }
                RSVPStatus.UNAVAILABLE -> {
                    btnUnavailable.setBackgroundTintList(itemView.context.getColorStateList(R.color.error))
                    btnUnavailable.setTextColor(itemView.context.getColor(R.color.white))
                }
                else -> {
                    // No RSVP selected yet
                }
            }
        }

        private fun setupClickListeners(notification: NotificationItem) {
            // Main item click
            itemView.setOnClickListener {
                onItemClick(notification)
            }
            
            // Action buttons
            deleteButton.setOnClickListener {
                onActionClick(notification, "delete")
            }

            markSeenButton.setOnClickListener {
                onActionClick(notification, "mark_seen")
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                diff < 604800000 -> "${diff / 86400000}d ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }

        private fun formatEventDateTime(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("EEE dd MMM • HH:mm", Locale.getDefault())
            return formatter.format(date)
        }

        private fun formatCountdown(countdownTime: Long): String {
            val now = System.currentTimeMillis()
            val diff = countdownTime - now
            
            return when {
                diff < 0 -> "Event started"
                diff < 3600000 -> "Starts in ${diff / 60000}m"
                diff < 86400000 -> "Starts in ${diff / 3600000}h"
                else -> "Starts in ${diff / 86400000}d"
            }
        }
    }
} 
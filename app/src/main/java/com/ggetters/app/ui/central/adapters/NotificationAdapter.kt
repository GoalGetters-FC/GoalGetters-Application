package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onActionClick: (NotificationItem, String) -> Unit,
    private val onItemClick: (NotificationItem) -> Unit
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
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        private val notificationSubtitle: TextView = itemView.findViewById(R.id.notificationSubtitle)
        private val notificationTimestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val markSeenButton: ImageButton = itemView.findViewById(R.id.markSeenButton)

        fun bind(notification: NotificationItem) {
            // Set notification content
            notificationTitle.text = notification.title
            notificationSubtitle.text = notification.subtitle
            notificationTimestamp.text = formatTimestamp(notification.timestamp)
            
            // Set icon based on notification type
            setNotificationIcon(notification.type)
            
            // Set background based on read status
            if (notification.isSeen) {
                itemView.setBackgroundResource(R.color.white)
                notificationTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
            } else {
                itemView.setBackgroundResource(R.color.surface_elevated)
                notificationTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            }

            // Set up click listeners
            itemView.setOnClickListener {
                onItemClick(notification)
            }

            deleteButton.setOnClickListener {
                onActionClick(notification, "delete")
            }

            markSeenButton.setOnClickListener {
                onActionClick(notification, "mark_seen")
            }
        }

        private fun setNotificationIcon(type: String) {
            val iconRes = when (type) {
                "game" -> R.drawable.ic_unicons_calender_24
                "practice" -> R.drawable.ic_unicons_clock_24
                "announcement" -> R.drawable.ic_unicons_bell_24
                "player" -> R.drawable.ic_unicons_user_24
                "admin" -> R.drawable.ic_unicons_settings_24
                else -> R.drawable.ic_unicons_bell_24
            }
            notificationIcon.setImageResource(iconRes)
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
    }
} 
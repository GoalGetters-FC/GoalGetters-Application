package com.ggetters.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.models.NotificationItem

class NotificationAdapter(
    private val notifications: List<NotificationItem>,
    private val onActionClick: (NotificationItem, String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

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
        private val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val markSeenButton: ImageButton = itemView.findViewById(R.id.markSeenButton)

        fun bind(notification: NotificationItem) {
            messageTextView.text = notification.message
            
            // Gray out if seen
            if (notification.isSeen) {
                messageTextView.alpha = 0.6f
            } else {
                messageTextView.alpha = 1.0f
            }

            deleteButton.setOnClickListener {
                onActionClick(notification, "delete")
            }

            markSeenButton.setOnClickListener {
                onActionClick(notification, "mark_seen")
            }
        }
    }
} 
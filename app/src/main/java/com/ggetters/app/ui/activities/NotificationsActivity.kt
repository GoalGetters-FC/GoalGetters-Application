package com.ggetters.app.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.adapters.NotificationAdapter
import com.ggetters.app.ui.models.NotificationItem

class NotificationsActivity : AppCompatActivity() {
    
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        setupToolbar()
        setupNotifications()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "All Notifications"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupNotifications() {
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Sample notifications data
        val notifications = listOf(
            NotificationItem(
                id = 1,
                message = "Team Practice scheduled for tomorrow at 3 PM",
                isSeen = false,
                type = "schedule"
            ),
            NotificationItem(
                id = 2,
                message = "Your team has a match this weekend",
                isSeen = true,
                type = "match"
            ),
            NotificationItem(
                id = 3,
                message = "John Doe has joined your team",
                isSeen = true,
                type = "team"
            ),
            NotificationItem(
                id = 4,
                message = "Training session moved to Friday",
                isSeen = true,
                type = "schedule"
            )
        )
        
        notificationAdapter = NotificationAdapter(notifications.toMutableList()) { notification, action ->
            when (action) {
                "delete" -> {
                    // TODO: Backend - Delete notification
                }
                "mark_seen" -> {
                    // TODO: Backend - Mark notification as seen
                    notification.isSeen = true
                    notificationAdapter.notifyDataSetChanged()
                }
            }
        }
        
        recyclerView.adapter = notificationAdapter
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 
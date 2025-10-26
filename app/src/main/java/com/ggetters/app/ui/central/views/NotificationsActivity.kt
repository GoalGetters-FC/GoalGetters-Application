package com.ggetters.app.ui.central.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.NotificationCardAdapter
import com.ggetters.app.data.model.Notification
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val model: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Enable smooth activity transitions
        supportPostponeEnterTransition()
        supportStartPostponedEnterTransition()

        setupLayoutUi()
        setupRecyclerView()
        observe()
        
        // Load notifications when activity starts
        model.loadNotifications()
    }

    // --- UI setup ---
    private fun setupLayoutUi() {
        enableEdgeToEdge()

        val appBar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Set up back button click listener
        appBar.setNavigationOnClickListener {
            finish()
        }
        
        val root = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        root.setBackgroundColor("#161620".toColorInt())
        
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
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
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notificationAdapter
    }

    // --- Observe ViewModel ---
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.notifications.collect { notifications ->
                    // Debug logging
                    com.ggetters.app.core.utils.Clogger.d("NotificationsActivity", "Received ${notifications.size} notifications from ViewModel")
                    notifications.forEach { notification ->
                        com.ggetters.app.core.utils.Clogger.d("NotificationsActivity", "Notification: ${notification.title} - ${notification.message}")
                    }
                    // Update adapter with new notifications
                    notificationAdapter.updateNotifications(notifications)
                    
                    // Force RecyclerView to refresh visually
                    val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
                    recyclerView?.invalidate()
                    recyclerView?.requestLayout()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.isLoading.collect { isLoading ->
                    // TODO: Show/hide loading indicator
                }
            }
        }

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
    }
} 
package com.ggetters.app.ui.central.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.ggetters.app.data.model.Notification
import com.ggetters.app.ui.central.adapters.NotificationCardAdapter
import com.ggetters.app.ui.central.viewmodels.NotificationsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val model: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationCardAdapter
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                model.loadNotifications()
            } else {
                showNotificationPermissionDeniedSnackbar(canOpenSettings = true)
                model.loadNotifications()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Enable smooth activity transitions
        supportPostponeEnterTransition()
        supportStartPostponedEnterTransition()

        setupLayoutUi()
        setupRecyclerView()
        observe()

        ensureNotificationPermission()
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
        
        // Set dark background on root (makes status bar dark via edge-to-edge)
        root.setBackgroundColor("#161620".toColorInt())
        
        // White status bar icons for dark background (matches TeamViewerActivity)
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
                // Mark as seen when clicked
                lifecycleScope.launch {
                    if (!notification.isSeen) {
                        model.markAsSeen(notification.id)
                    }
                }
            },
            onMarkAsSeen = { notification ->
                // Mark as seen
                lifecycleScope.launch {
                    model.markAsSeen(notification.id)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Notification marked as ${if (notification.isSeen) "unread" else "read"}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            },
            onDelete = { notification ->
                // Delete notification with confirmation feedback
                lifecycleScope.launch {
                    model.deleteNotification(notification.id)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Notification deleted",
                        Snackbar.LENGTH_SHORT
                    ).setAction("UNDO") {
                        // TODO: Implement undo functionality if needed
                    }.show()
                }
            }
        )
        val recyclerView = findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notificationAdapter
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            model.loadNotifications()
            return
        }

        val permissionStatus = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )

        when {
            permissionStatus == PackageManager.PERMISSION_GRANTED -> {
                model.loadNotifications()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.notification_permission_rationale,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.notification_permission_enable_action) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }.show()
                model.loadNotifications()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // --- Observe ViewModel ---
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.notifications.collect { notifications ->
                    // Debug logging
                    com.ggetters.app.core.utils.Clogger.d("NotificationsActivity", "Received ${notifications.size} notifications from ViewModel")
                    
                    // Update adapter with new notifications
                    notificationAdapter.updateNotifications(notifications)
                    
                    // Show/hide empty state
                    val emptyState = findViewById<LinearLayout>(R.id.emptyState)
                    val scrollContainer = findViewById<androidx.core.widget.NestedScrollView>(R.id.svContainer)
                    
                    if (notifications.isEmpty()) {
                        emptyState?.visibility = android.view.View.VISIBLE
                        scrollContainer?.visibility = android.view.View.GONE
                    } else {
                        emptyState?.visibility = android.view.View.GONE
                        scrollContainer?.visibility = android.view.View.VISIBLE
                    }
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

    private fun showNotificationPermissionDeniedSnackbar(canOpenSettings: Boolean) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            R.string.notification_permission_denied,
            Snackbar.LENGTH_LONG
        )

        if (canOpenSettings && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            snackbar.setAction(R.string.notification_permission_settings_action) {
                openNotificationSettings()
            }
        }

        snackbar.show()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }
} 
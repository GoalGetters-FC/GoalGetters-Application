package com.ggetters.app.core.services

import com.ggetters.app.data.model.Notification
import com.ggetters.app.core.utils.Clogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Event bus for notifying about new notifications across the app
 * Uses a persistent scope to ensure notifications are delivered even when activities are stopped
 */
object NotificationEventBus {
    private val _newNotificationEvents = MutableSharedFlow<Notification>(
        replay = 1, // Replay the last notification for late subscribers
        extraBufferCapacity = 10 // Buffer up to 10 notifications
    )
    val newNotificationEvents: SharedFlow<Notification> = _newNotificationEvents.asSharedFlow()
    
    // Use a persistent scope that survives activity lifecycle changes
    private val persistentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    suspend fun notifyNewNotification(notification: Notification) {
        Clogger.d("NotificationEventBus", "Received notification to emit: ${notification.title}")
        _newNotificationEvents.emit(notification)
        Clogger.d("NotificationEventBus", "Notification emitted successfully: ${notification.title}")
    }
    
    // Non-suspend version for use from non-suspend contexts
    fun notifyNewNotificationAsync(notification: Notification) {
        persistentScope.launch {
            notifyNewNotification(notification)
        }
    }
}

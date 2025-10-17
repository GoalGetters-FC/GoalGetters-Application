package com.ggetters.app.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationPriority
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.repository.notification.NotificationRepository
import com.ggetters.app.ui.central.views.NotificationsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "NotificationService"
        private const val CHANNEL_ID = "goal_getters_notifications"
        private const val CHANNEL_NAME = "Goal Getters Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for matches, practices, and team updates"
    }

    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Clogger.d(TAG, "Received FCM message: ${remoteMessage.messageId}")
        
        // Handle data payload
        remoteMessage.data.let { data ->
            val title = data["title"] ?: "Goal Getters"
            val body = data["body"] ?: "You have a new notification"
            val type = data["type"] ?: "SYSTEM"
            val priority = data["priority"] ?: "NORMAL"
            val linkedEventId = data["linkedEventId"]
            val linkedEventType = data["linkedEventType"]
            
            // Create notification object
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                title = title,
                subtitle = "",
                message = body,
                type = NotificationType.valueOf(type),
                priority = NotificationPriority.valueOf(priority),
                userId = firebaseAuth.currentUser?.uid ?: "",
                linkedEventId = linkedEventId,
                linkedEventType = linkedEventType?.let { 
                    try {
                        com.ggetters.app.data.model.LinkedEventType.valueOf(it)
                    } catch (e: Exception) {
                        null
                    }
                },
                createdAt = Instant.now()
            )
            
            // Store notification locally
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    notificationRepository.upsert(notification)
                    Clogger.d(TAG, "Notification stored locally: ${notification.id}")
                } catch (e: Exception) {
                    Clogger.e(TAG, "Failed to store notification locally", e)
                }
            }
            
            // Show system notification
            showSystemNotification(notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Clogger.d(TAG, "FCM token refreshed: $token")
        
        // TODO: Send token to server for user registration
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Register token with server
                registerTokenWithServer(token)
                // Subscribe to topics for current user/team
                firebaseAuth.currentUser?.let { user ->
                    try {
                        // Basic topic strategy: per-user and global
                        FirebaseMessaging.getInstance().subscribeToTopic("user_${'$'}{user.uid}").await()
                        FirebaseMessaging.getInstance().subscribeToTopic("goal_getters").await()
                        Clogger.d(TAG, "Subscribed to topics for user ${'$'}{user.uid}")
                    } catch (e: Exception) {
                        Clogger.e(TAG, "Failed subscribing to topics", e)
                    }
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to register token with server", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(notification: Notification) {
        val intent = Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_unicons_soccer_24)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(getNotificationPriority(notification.priority))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        // Add action buttons for actionable notifications
        if (notification.isActionable()) {
            val actionIntent = Intent(this, NotificationsActivity::class.java).apply {
                putExtra("notification_id", notification.id)
                putExtra("action", "view")
            }
            
            val actionPendingIntent = PendingIntent.getActivity(
                this,
                notification.id.hashCode(),
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            notificationBuilder.addAction(
                R.drawable.ic_unicons_eye_24,
                "View",
                actionPendingIntent
            )
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id.hashCode(), notificationBuilder.build())
    }

    private fun getNotificationPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_MAX
        }
    }

    private suspend fun registerTokenWithServer(token: String) {
        // Persist token under users/{uid}/fcmTokens/{token}
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("fcmTokens")
                .document(token)
                .set(mapOf(
                    "token" to token,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "platform" to "android"
                ))
                .await()
            Clogger.d(TAG, "Token saved to Firestore for user ${'$'}uid")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to save token to Firestore", e)
            throw e
        }
    }
}

/**
 * Service for managing notifications and FCM operations
 */
class NotificationManagerService @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "NotificationManagerService"
    }

    /**
     * Subscribe to FCM topics for a user
     */
    suspend fun subscribeToTopics(userId: String, teamId: String?) {
        try {
            // Subscribe to user-specific topic
            FirebaseMessaging.getInstance().subscribeToTopic("user_$userId").await()
            
            // Subscribe to team-specific topic if available
            teamId?.let {
                FirebaseMessaging.getInstance().subscribeToTopic("team_$it").await()
            }
            
            // Subscribe to general app topic
            FirebaseMessaging.getInstance().subscribeToTopic("goal_getters").await()
            
            Clogger.d(TAG, "Subscribed to FCM topics for user $userId")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to subscribe to FCM topics", e)
        }
    }

    /**
     * Unsubscribe from FCM topics for a user
     */
    suspend fun unsubscribeFromTopics(userId: String, teamId: String?) {
        try {
            // Unsubscribe from user-specific topic
            FirebaseMessaging.getInstance().unsubscribeFromTopic("user_$userId")
            
            // Unsubscribe from team-specific topic if available
            teamId?.let {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("team_$it")
            }
            
            // Unsubscribe from general app topic
            FirebaseMessaging.getInstance().unsubscribeFromTopic("goal_getters")
            
            Clogger.d(TAG, "Unsubscribed from FCM topics for user $userId")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to unsubscribe from FCM topics", e)
        }
    }

    /**
     * Send a notification to a specific user
     */
    suspend fun sendNotificationToUser(userId: String, notification: Notification): String {
        return notificationRepository.sendToUser(userId, notification)
    }

    /**
     * Send a notification to all users in a team
     */
    suspend fun sendNotificationToTeam(teamId: String, notification: Notification): List<String> {
        return notificationRepository.sendToTeam(teamId, notification)
    }

    /**
     * Get unread notification count for current user
     */
    suspend fun getUnreadCount(): Int {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return 0
        return notificationRepository.getUnreadCount(currentUserId)
    }
}

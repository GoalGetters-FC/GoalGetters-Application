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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    
    companion object {
        private const val TAG = "LocalNotificationService"
        private const val CHANNEL_ID = "goal_getters_local_notifications"
        private const val CHANNEL_NAME = "Goal Getters Local Notifications"
        private const val CHANNEL_DESCRIPTION = "Local notifications for matches, practices, and team updates"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val _notifications = MutableSharedFlow<Notification>()
    val notifications: SharedFlow<Notification> = _notifications.asSharedFlow()

    init {
        createNotificationChannel()
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
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create and send a local notification
     */
    suspend fun createNotification(
        title: String,
        message: String,
        type: NotificationType,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        userId: String,
        teamId: String? = null,
        linkedEventId: String? = null,
        linkedEventType: com.ggetters.app.data.model.LinkedEventType? = null,
        data: Map<String, Any> = emptyMap()
    ) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            title = title,
            subtitle = "",
            message = message,
            type = type,
            priority = priority,
            userId = userId,
            teamId = teamId,
            linkedEventId = linkedEventId,
            linkedEventType = linkedEventType,
            data = JSONObject(data).toString(), // JSON string for storage
            createdAt = Instant.now()
        )

        try {
            // Store in database
            notificationRepository.upsert(notification)
            
            // Emit to flow for real-time updates
            _notifications.emit(notification)
            
            // Notify the ViewModel about the new notification via EventBus
            // This ensures local notifications also appear in the notifications fragment
            Clogger.d(TAG, "Notifying NotificationEventBus about local notification: ${notification.title}")
            com.ggetters.app.core.services.NotificationEventBus.notifyNewNotificationAsync(notification)
            Clogger.d(TAG, "NotificationEventBus notification sent successfully")
            
            // Show system notification
            showSystemNotification(notification)
            
            Clogger.d(TAG, "Local notification created: ${notification.id}")
            Clogger.d(TAG, "Notification details: title='${notification.title}', message='${notification.message}', userId='${notification.userId}', teamId='${notification.teamId}'")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to create local notification", e)
        }
    }

    /**
     * Create automated notifications based on events
     */
    suspend fun createMatchReminder(
        eventTitle: String,
        eventTime: String,
        venue: String,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        createNotification(
            title = "Match Reminder",
            message = "Don't forget: $eventTitle at $eventTime ($venue)",
            type = NotificationType.GAME_REMINDER,
            priority = NotificationPriority.HIGH,
            userId = userId,
            teamId = teamId,
            linkedEventId = eventId,
            linkedEventType = com.ggetters.app.data.model.LinkedEventType.GAME,
            data = mapOf(
                "eventTitle" to eventTitle,
                "eventTime" to eventTime,
                "venue" to venue
            )
        )
    }

    suspend fun createPracticeReminder(
        eventTitle: String,
        eventTime: String,
        venue: String,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        createNotification(
            title = "Practice Reminder",
            message = "Don't forget: $eventTitle at $eventTime ($venue)",
            type = NotificationType.PRACTICE_REMINDER,
            priority = NotificationPriority.NORMAL,
            userId = userId,
            teamId = teamId,
            linkedEventId = eventId,
            linkedEventType = com.ggetters.app.data.model.LinkedEventType.PRACTICE,
            data = mapOf(
                "eventTitle" to eventTitle,
                "eventTime" to eventTime,
                "venue" to venue
            )
        )
    }

    suspend fun createMatchResult(
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        val isWin = homeScore > awayScore
        val resultText = if (isWin) "Victory!" else "Match Complete"
        
        createNotification(
            title = "Match Result: $resultText",
            message = "$homeTeam $homeScore - $awayScore $awayTeam",
            type = NotificationType.POST_MATCH_SUMMARY,
            priority = NotificationPriority.HIGH,
            userId = userId,
            teamId = teamId,
            linkedEventId = eventId,
            linkedEventType = com.ggetters.app.data.model.LinkedEventType.MATCH_RESULTS,
            data = mapOf(
                "homeTeam" to homeTeam,
                "awayTeam" to awayTeam,
                "homeScore" to homeScore,
                "awayScore" to awayScore,
                "isWin" to isWin
            )
        )
    }

    suspend fun createScheduleChange(
        eventTitle: String,
        oldTime: String,
        newTime: String,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        createNotification(
            title = "Schedule Change",
            message = "$eventTitle time changed from $oldTime to $newTime",
            type = NotificationType.SCHEDULE_CHANGE,
            priority = NotificationPriority.HIGH,
            userId = userId,
            teamId = teamId,
            linkedEventId = eventId,
            data = mapOf(
                "eventTitle" to eventTitle,
                "oldTime" to oldTime,
                "newTime" to newTime
            )
        )
    }

    suspend fun createAnnouncement(
        title: String,
        message: String,
        userId: String,
        teamId: String
    ) {
        createNotification(
            title = title,
            message = message,
            type = NotificationType.ANNOUNCEMENT,
            priority = NotificationPriority.NORMAL,
            userId = userId,
            teamId = teamId
        )
    }

    private fun showSystemNotification(notification: Notification) {
        val intent = Intent(context, NotificationsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_unicons_bell_24)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(
                when (notification.priority) {
                    NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                    NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
                    NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    /**
     * Mark notification as seen
     */
    suspend fun markAsSeen(notificationId: String) {
        try {
            val notification = notificationRepository.getById(notificationId)
            if (notification != null) {
                val updatedNotification = notification.copy(isSeen = true)
                notificationRepository.upsert(updatedNotification)
                _notifications.emit(updatedNotification)
            }
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to mark notification as seen", e)
        }
    }

    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String) {
        try {
            val notification = notificationRepository.getById(notificationId)
            if (notification != null) {
                notificationRepository.delete(notification)
                Clogger.d(TAG, "Notification deleted: $notificationId")
            }
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to delete notification", e)
        }
    }
}

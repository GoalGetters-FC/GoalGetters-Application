package com.ggetters.app.core.services

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.NotificationPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTestService @Inject constructor(
    private val localNotificationService: LocalNotificationService
) {
    
    companion object {
        private const val TAG = "NotificationTestService"
    }

    /**
     * Create sample notifications for testing
     */
    fun createSampleNotifications(userId: String, teamId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sample reminder notification
                delay(1000)
                localNotificationService.createNotification(
                    title = "Don't forget to pack your shin guards for tomorrow!",
                    message = "Match equipment reminder",
                    type = NotificationType.GAME_REMINDER,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId,
                    data = mapOf(
                        "equipment" to "shin guards",
                        "event" to "match"
                    )
                )

                // Sample match result notification
                delay(2000)
                localNotificationService.createNotification(
                    title = "Summary of results.",
                    message = "Match completed",
                    type = NotificationType.POST_MATCH_SUMMARY,
                    priority = NotificationPriority.HIGH,
                    userId = userId,
                    teamId = teamId,
                    data = mapOf(
                        "homeTeam" to "Arsenal",
                        "awayTeam" to "Liverpool",
                        "homeScore" to 15,
                        "awayScore" to 2,
                        "isWin" to true
                    )
                )

                // Sample practice notification
                delay(3000)
                localNotificationService.createNotification(
                    title = "New practice scheduled!",
                    message = "Practice session added to calendar",
                    type = NotificationType.PRACTICE_NOTIFICATION,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId,
                    data = mapOf(
                        "eventDate" to "12/06",
                        "eventTime" to "21:00"
                    )
                )

                // Sample announcement notification
                delay(4000)
                localNotificationService.createNotification(
                    title = "Parents: remember to pick up your players from the front not the back. Thanks.",
                    message = "Important pickup information",
                    type = NotificationType.ANNOUNCEMENT,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId
                )

                Clogger.d(TAG, "Sample notifications created successfully")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create sample notifications", e)
            }
        }
    }
}

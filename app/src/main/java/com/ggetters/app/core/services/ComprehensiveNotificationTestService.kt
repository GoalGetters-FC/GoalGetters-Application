package com.ggetters.app.core.services

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.NotificationPriority
import com.ggetters.app.data.model.LinkedEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComprehensiveNotificationTestService @Inject constructor(
    private val localNotificationService: LocalNotificationService,
    private val eventNotificationService: EventNotificationService,
    private val notificationIntegrationService: NotificationIntegrationService
) {
    
    companion object {
        private const val TAG = "ComprehensiveNotificationTestService"
    }

    /**
     * Test all notification types comprehensively
     */
    fun runComprehensiveNotificationTest(userId: String, teamId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Clogger.d(TAG, "Starting comprehensive notification test...")

                // 1. Test Event Notifications
                testEventNotifications(userId, teamId)
                delay(2000)

                // 2. Test Match Event Notifications
                testMatchEventNotifications(userId, teamId)
                delay(2000)

                // 3. Test Reminder Notifications
                testReminderNotifications(userId, teamId)
                delay(2000)

                // 4. Test Tournament Notifications
                testTournamentNotifications(userId, teamId)
                delay(2000)

                // 5. Test Training Notifications
                testTrainingNotifications(userId, teamId)
                delay(2000)

                // 6. Test Friendly Match Notifications
                testFriendlyMatchNotifications(userId, teamId)
                delay(2000)

                // 7. Test Schedule Change Notifications
                testScheduleChangeNotifications(userId, teamId)
                delay(2000)

                // 8. Test Announcement Notifications
                testAnnouncementNotifications(userId, teamId)

                Clogger.d(TAG, "Comprehensive notification test completed!")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to run comprehensive notification test", e)
            }
        }
    }

    private suspend fun testEventNotifications(userId: String, teamId: String) {
        // Test Practice Event
        val practiceEvent = Event(
            id = UUID.randomUUID().toString(),
            teamId = teamId,
            name = "Weekly Practice Session",
            category = EventCategory.PRACTICE,
            style = EventStyle.STANDARD,
            startAt = LocalDateTime.now().plusDays(1),
            location = "Main Field"
        )
        eventNotificationService.createEventCreatedNotification(practiceEvent, userId, teamId)

        // Test Match Event
        val matchEvent = Event(
            id = UUID.randomUUID().toString(),
            teamId = teamId,
            name = "Championship Final",
            category = EventCategory.MATCH,
            style = EventStyle.TOURNAMENT,
            startAt = LocalDateTime.now().plusDays(3),
            location = "Stadium"
        )
        eventNotificationService.createEventCreatedNotification(matchEvent, userId, teamId)
    }

    private suspend fun testMatchEventNotifications(userId: String, teamId: String) {
        // Test Goal Notification
        localNotificationService.createNotification(
            title = "Goal!",
            message = "Goal scored by John Smith at 23'",
            type = NotificationType.GAME_NOTIFICATION,
            priority = NotificationPriority.HIGH,
            userId = userId,
            teamId = teamId,
            data = mapOf(
                "eventType" to "GOAL",
                "playerName" to "John Smith",
                "minute" to 23
            )
        )

        // Test Substitution Notification
        localNotificationService.createNotification(
            title = "Substitution",
            message = "Mike Johnson replaced by Alex Brown at 67'",
            type = NotificationType.GAME_NOTIFICATION,
            priority = NotificationPriority.NORMAL,
            userId = userId,
            teamId = teamId,
            data = mapOf(
                "eventType" to "SUBSTITUTION",
                "playerOut" to "Mike Johnson",
                "playerIn" to "Alex Brown",
                "minute" to 67
            )
        )

        // Test Yellow Card Notification
        localNotificationService.createNotification(
            title = "Yellow Card",
            message = "Yellow card for David Wilson at 45'",
            type = NotificationType.GAME_NOTIFICATION,
            priority = NotificationPriority.NORMAL,
            userId = userId,
            teamId = teamId,
            data = mapOf(
                "eventType" to "YELLOW_CARD",
                "playerName" to "David Wilson",
                "minute" to 45
            )
        )
    }

    private suspend fun testReminderNotifications(userId: String, teamId: String) {
        // Test Match Reminder
        localNotificationService.createMatchReminder(
            eventTitle = "Arsenal vs Liverpool",
            eventTime = "15:00",
            venue = "Emirates Stadium",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )

        // Test Practice Reminder
        localNotificationService.createPracticeReminder(
            eventTitle = "Training Session",
            eventTime = "18:00",
            venue = "Training Ground",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )
    }

    private suspend fun testTournamentNotifications(userId: String, teamId: String) {
        eventNotificationService.createTournamentNotification(
            tournamentName = "Champions League",
            message = "Quarter-final draw has been announced!",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )
    }

    private suspend fun testTrainingNotifications(userId: String, teamId: String) {
        eventNotificationService.createTrainingNotification(
            trainingName = "Fitness Training",
            message = "Focus on endurance and strength building",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )
    }

    private suspend fun testFriendlyMatchNotifications(userId: String, teamId: String) {
        eventNotificationService.createFriendlyMatchNotification(
            matchName = "Friendly Match",
            opponent = "Local Rivals FC",
            message = "Pre-season preparation match",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )
    }

    private suspend fun testScheduleChangeNotifications(userId: String, teamId: String) {
        localNotificationService.createScheduleChange(
            eventTitle = "Practice Session",
            oldTime = "18:00",
            newTime = "19:00",
            userId = userId,
            teamId = teamId,
            eventId = UUID.randomUUID().toString()
        )
    }

    private suspend fun testAnnouncementNotifications(userId: String, teamId: String) {
        localNotificationService.createAnnouncement(
            title = "Team Meeting",
            message = "Important team meeting scheduled for tomorrow at 6 PM. All players must attend.",
            userId = userId,
            teamId = teamId
        )
    }

    /**
     * Test match result notifications with different outcomes
     */
    fun testMatchResultNotifications(userId: String, teamId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Test Win
                localNotificationService.createMatchResult(
                    homeTeam = "Arsenal",
                    awayTeam = "Liverpool",
                    homeScore = 3,
                    awayScore = 1,
                    userId = userId,
                    teamId = teamId,
                    eventId = UUID.randomUUID().toString()
                )
                delay(1000)

                // Test Loss
                localNotificationService.createMatchResult(
                    homeTeam = "Arsenal",
                    awayTeam = "Manchester City",
                    homeScore = 0,
                    awayScore = 2,
                    userId = userId,
                    teamId = teamId,
                    eventId = UUID.randomUUID().toString()
                )
                delay(1000)

                // Test Draw
                localNotificationService.createMatchResult(
                    homeTeam = "Arsenal",
                    awayTeam = "Chelsea",
                    homeScore = 1,
                    awayScore = 1,
                    userId = userId,
                    teamId = teamId,
                    eventId = UUID.randomUUID().toString()
                )

                Clogger.d(TAG, "Match result notifications test completed")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to test match result notifications", e)
            }
        }
    }
}

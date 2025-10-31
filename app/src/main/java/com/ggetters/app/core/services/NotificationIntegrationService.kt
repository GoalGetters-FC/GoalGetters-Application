package com.ggetters.app.core.services

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.NotificationPriority
import com.ggetters.app.data.repository.match.MatchEventRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationIntegrationService @Inject constructor(
    private val localNotificationService: LocalNotificationService,
    private val matchEventRepository: MatchEventRepository,
    private val userRepository: UserRepository
) {
    
    companion object {
        private const val TAG = "NotificationIntegrationService"
    }

    /**
     * Create notifications for match events
     */
    fun createMatchEventNotification(
        event: MatchEvent,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (event.eventType) {
                    MatchEventType.GOAL -> {
                        if (event.details["isOpponentGoal"] == true) {
                            localNotificationService.createNotification(
                                title = "Opponent Goal",
                                message = "Opponent scored at ${event.minute}'",
                                type = NotificationType.GAME_NOTIFICATION,
                                priority = NotificationPriority.HIGH,
                                userId = userId,
                                teamId = teamId,
                                linkedEventId = event.matchId,
                            data = mapOf(
                                "eventType" to "OPPONENT_GOAL",
                                "minute" to event.minute,
                                "isOpponent" to true
                            ) as Map<String, Any>
                            )
                        } else {
                            localNotificationService.createNotification(
                                title = "Goal!",
                                message = "Goal scored by ${event.playerName} at ${event.minute}'",
                                type = NotificationType.GAME_NOTIFICATION,
                                priority = NotificationPriority.HIGH,
                                userId = userId,
                                teamId = teamId,
                                linkedEventId = event.matchId,
                                data = mapOf(
                                    "eventType" to "GOAL",
                                    "playerName" to (event.playerName ?: ""),
                                    "minute" to event.minute
                                ) as Map<String, Any>
                            )
                        }
                    }
                    MatchEventType.SUBSTITUTION -> {
                        localNotificationService.createNotification(
                            title = "Substitution",
                            message = "${event.details["playerOut"]} replaced by ${event.details["playerIn"]} at ${event.minute}'",
                            type = NotificationType.GAME_NOTIFICATION,
                            priority = NotificationPriority.NORMAL,
                            userId = userId,
                            teamId = teamId,
                            linkedEventId = event.matchId,
                            data = mapOf(
                                "eventType" to "SUBSTITUTION",
                                "playerOut" to (event.details["playerOut"] ?: ""),
                                "playerIn" to (event.details["playerIn"] ?: ""),
                                "minute" to event.minute
                            ) as Map<String, Any>
                        )
                    }
                    MatchEventType.YELLOW_CARD -> {
                        localNotificationService.createNotification(
                            title = "Yellow Card",
                            message = "Yellow card for ${event.playerName} at ${event.minute}'",
                            type = NotificationType.GAME_NOTIFICATION,
                            priority = NotificationPriority.NORMAL,
                            userId = userId,
                            teamId = teamId,
                            linkedEventId = event.matchId,
                            data = mapOf(
                                "eventType" to "YELLOW_CARD",
                                "playerName" to (event.playerName ?: ""),
                                "minute" to event.minute
                            ) as Map<String, Any>
                        )
                    }
                    MatchEventType.RED_CARD -> {
                        localNotificationService.createNotification(
                            title = "Red Card",
                            message = "Red card for ${event.playerName} at ${event.minute}'",
                            type = NotificationType.GAME_NOTIFICATION,
                            priority = NotificationPriority.HIGH,
                            userId = userId,
                            teamId = teamId,
                            linkedEventId = event.matchId,
                            data = mapOf(
                                "eventType" to "RED_CARD",
                                "playerName" to (event.playerName ?: ""),
                                "minute" to event.minute
                            ) as Map<String, Any>
                        )
                    }
                    else -> {
                        // Handle other event types if needed
                        Clogger.d(TAG, "Unhandled match event type: ${event.eventType}")
                    }
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create match event notification", e)
            }
        }
    }

    /**
     * Create match result notification
     */
    fun createMatchResultNotification(
        matchId: String,
        homeScore: Int,
        awayScore: Int,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createMatchResult(
                    homeTeam = "Home Team",
                    awayTeam = "Away Team",
                    homeScore = homeScore,
                    awayScore = awayScore,
                    userId = userId,
                    teamId = teamId,
                    eventId = matchId
                )
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create match result notification", e)
            }
        }
    }

    /**
     * Create match reminder notification
     */
    fun createMatchReminderNotification(
        matchId: String,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createMatchReminder(
                    eventTitle = "Match vs Opponent",
                    eventTime = "15:00",
                    venue = "Stadium",
                    userId = userId,
                    teamId = teamId,
                    eventId = matchId
                )
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create match reminder notification", e)
            }
        }
    }

    /**
     * Create practice reminder notification
     */
    fun createPracticeReminderNotification(
        practiceId: String,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createPracticeReminder(
                    eventTitle = "Practice Session",
                    eventTime = "18:00",
                    venue = "Training Ground",
                    userId = userId,
                    teamId = teamId,
                    eventId = practiceId
                )
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create practice reminder notification", e)
            }
        }
    }

    /**
     * Create schedule change notification
     */
    fun createScheduleChangeNotification(
        eventId: String,
        oldTime: String,
        newTime: String,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createScheduleChange(
                    eventTitle = "Match vs Opponent",
                    oldTime = oldTime,
                    newTime = newTime,
                    userId = userId,
                    teamId = teamId,
                    eventId = eventId
                )
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create schedule change notification", e)
            }
        }
    }

    /**
     * Create team announcement notification
     */
    fun createTeamAnnouncementNotification(
        title: String,
        message: String,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createAnnouncement(
                    title = title,
                    message = message,
                    userId = userId,
                    teamId = teamId
                )
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create team announcement notification", e)
            }
        }
    }
}

package com.ggetters.app.core.services

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.NotificationPriority
import com.ggetters.app.data.model.LinkedEventType
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventNotificationService @Inject constructor(
    private val localNotificationService: LocalNotificationService,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {
    
    companion object {
        private const val TAG = "EventNotificationService"
    }

    /**
     * Create notification when a new event is created
     */
    fun createEventCreatedNotification(
        event: Event,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationType = when (event.category) {
                    EventCategory.MATCH -> NotificationType.GAME_NOTIFICATION
                    EventCategory.PRACTICE -> NotificationType.PRACTICE_NOTIFICATION
                    EventCategory.TRAINING -> NotificationType.PRACTICE_NOTIFICATION
                    else -> NotificationType.ANNOUNCEMENT
                }

                val eventTypeText = when (event.category) {
                    EventCategory.MATCH -> "Game"
                    EventCategory.PRACTICE -> "Practice"
                    EventCategory.TRAINING -> "Training"
                    else -> "Event"
                }

                localNotificationService.createNotification(
                    title = "New $eventTypeText Scheduled!",
                    message = "${event.name} - ${event.startAt}",
                    type = notificationType,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = event.id,
                    linkedEventType = when (event.category) {
                        EventCategory.MATCH -> LinkedEventType.GAME
                        EventCategory.PRACTICE, EventCategory.TRAINING -> LinkedEventType.PRACTICE
                        else -> LinkedEventType.ANNOUNCEMENT
                    },
                    data = mapOf(
                        "eventId" to event.id,
                        "eventName" to event.name,
                        "eventCategory" to event.category.name,
                        "eventStyle" to event.style.name,
                        "startAt" to event.startAt.toString(),
                        "location" to (event.location ?: "TBD")
                    )
                )

                // Schedule reminder notifications
                scheduleEventReminders(event, userId, teamId)

                Clogger.d(TAG, "Event created notification sent for: ${event.name}")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create event notification", e)
            }
        }
    }

    /**
     * Schedule reminder notifications for an event
     */
    private suspend fun scheduleEventReminders(
        event: Event,
        userId: String,
        teamId: String
    ) {
        val now = LocalDateTime.now()
        val eventTime = event.startAt
        val hoursUntilEvent = ChronoUnit.HOURS.between(now, eventTime)
        val minutesUntilEvent = ChronoUnit.MINUTES.between(now, eventTime)

        // Schedule 24-hour reminder
        if (hoursUntilEvent >= 24) {
            val reminderTime = eventTime.minusHours(24)
            scheduleReminderNotification(
                event = event,
                reminderTime = reminderTime,
                message = "24 hours until ${event.name}!",
                userId = userId,
                teamId = teamId
            )
        }

        // Schedule 2-hour reminder
        if (hoursUntilEvent >= 2) {
            val reminderTime = eventTime.minusHours(2)
            scheduleReminderNotification(
                event = event,
                reminderTime = reminderTime,
                message = "2 hours until ${event.name}!",
                userId = userId,
                teamId = teamId
            )
        }

        // Schedule 30-minute reminder
        if (minutesUntilEvent >= 30) {
            val reminderTime = eventTime.minusMinutes(30)
            scheduleReminderNotification(
                event = event,
                reminderTime = reminderTime,
                message = "30 minutes until ${event.name}!",
                userId = userId,
                teamId = teamId
            )
        }
    }

    /**
     * Schedule a reminder notification for a specific time
     */
    private fun scheduleReminderNotification(
        event: Event,
        reminderTime: LocalDateTime,
        message: String,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = LocalDateTime.now()
                val delayMinutes = ChronoUnit.MINUTES.between(now, reminderTime)
                
                if (delayMinutes > 0) {
                    delay(delayMinutes * 60 * 1000) // Convert to milliseconds
                    
                    val notificationType = when (event.category) {
                        EventCategory.MATCH -> NotificationType.GAME_REMINDER
                        EventCategory.PRACTICE, EventCategory.TRAINING -> NotificationType.PRACTICE_REMINDER
                        else -> NotificationType.ANNOUNCEMENT
                    }

                    localNotificationService.createNotification(
                        title = message,
                        message = "${event.name} at ${event.startAt} (${event.location ?: "TBD"})",
                        type = notificationType,
                        priority = NotificationPriority.HIGH,
                        userId = userId,
                        teamId = teamId,
                        linkedEventId = event.id,
                        linkedEventType = when (event.category) {
                            EventCategory.MATCH -> LinkedEventType.GAME
                            EventCategory.PRACTICE, EventCategory.TRAINING -> LinkedEventType.PRACTICE
                            else -> LinkedEventType.ANNOUNCEMENT
                        },
                        data = mapOf(
                            "eventId" to event.id,
                            "eventName" to event.name,
                            "eventCategory" to event.category.name,
                            "startAt" to event.startAt.toString(),
                            "location" to (event.location ?: "TBD"),
                            "reminderType" to "scheduled"
                        )
                    )
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to schedule reminder notification", e)
            }
        }
    }

    /**
     * Create notification when an event is updated
     */
    fun createEventUpdatedNotification(
        event: Event,
        changes: List<String>,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val changesText = changes.joinToString(", ")
                
                localNotificationService.createNotification(
                    title = "Event Updated: ${event.name}",
                    message = "Changes: $changesText",
                    type = NotificationType.SCHEDULE_CHANGE,
                    priority = NotificationPriority.HIGH,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = event.id,
                    linkedEventType = when (event.category) {
                        EventCategory.MATCH -> LinkedEventType.GAME
                        EventCategory.PRACTICE, EventCategory.TRAINING -> LinkedEventType.PRACTICE
                        else -> LinkedEventType.ANNOUNCEMENT
                    },
                    data = mapOf(
                        "eventId" to event.id,
                        "eventName" to event.name,
                        "changes" to changes,
                        "eventCategory" to event.category.name
                    )
                )

                Clogger.d(TAG, "Event updated notification sent for: ${event.name}")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create event updated notification", e)
            }
        }
    }

    /**
     * Create notification when an event is cancelled
     */
    fun createEventCancelledNotification(
        event: Event,
        reason: String? = null,
        userId: String,
        teamId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reasonText = reason?.let { " Reason: $it" } ?: ""
                
                localNotificationService.createNotification(
                    title = "Event Cancelled: ${event.name}",
                    message = "This event has been cancelled.$reasonText",
                    type = NotificationType.SCHEDULE_CHANGE,
                    priority = NotificationPriority.HIGH,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = event.id,
                    data = mapOf(
                        "eventId" to event.id,
                        "eventName" to event.name,
                        "cancellationReason" to (reason ?: "No reason provided"),
                        "eventCategory" to event.category.name
                    )
                )

                Clogger.d(TAG, "Event cancelled notification sent for: ${event.name}")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create event cancelled notification", e)
            }
        }
    }

    /**
     * Create notification for tournament events
     */
    fun createTournamentNotification(
        tournamentName: String,
        message: String,
        userId: String,
        teamId: String,
        eventId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createNotification(
                    title = "Tournament Update: $tournamentName",
                    message = message,
                    type = NotificationType.ANNOUNCEMENT,
                    priority = NotificationPriority.HIGH,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = eventId,
                    linkedEventType = LinkedEventType.GAME,
                    data = mapOf(
                        "tournamentName" to tournamentName,
                        "eventType" to "TOURNAMENT",
                        "eventId" to (eventId ?: "")
                    )
                )

                Clogger.d(TAG, "Tournament notification sent: $tournamentName")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create tournament notification", e)
            }
        }
    }

    /**
     * Create notification for training events
     */
    fun createTrainingNotification(
        trainingName: String,
        message: String,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createNotification(
                    title = "Training Session: $trainingName",
                    message = message,
                    type = NotificationType.PRACTICE_NOTIFICATION,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = eventId,
                    linkedEventType = LinkedEventType.PRACTICE,
                    data = mapOf(
                        "trainingName" to trainingName,
                        "eventType" to "TRAINING",
                        "eventId" to eventId
                    )
                )

                Clogger.d(TAG, "Training notification sent: $trainingName")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create training notification", e)
            }
        }
    }

    /**
     * Create notification for friendly matches
     */
    fun createFriendlyMatchNotification(
        matchName: String,
        opponent: String,
        message: String,
        userId: String,
        teamId: String,
        eventId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localNotificationService.createNotification(
                    title = "Friendly Match: $matchName",
                    message = "vs $opponent - $message",
                    type = NotificationType.GAME_NOTIFICATION,
                    priority = NotificationPriority.NORMAL,
                    userId = userId,
                    teamId = teamId,
                    linkedEventId = eventId,
                    linkedEventType = LinkedEventType.GAME,
                    data = mapOf(
                        "matchName" to matchName,
                        "opponent" to opponent,
                        "eventType" to "FRIENDLY_MATCH",
                        "eventId" to eventId
                    )
                )

                Clogger.d(TAG, "Friendly match notification sent: $matchName")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to create friendly match notification", e)
            }
        }
    }
}



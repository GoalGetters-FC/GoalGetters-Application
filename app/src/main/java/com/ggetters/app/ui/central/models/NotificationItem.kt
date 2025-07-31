package com.ggetters.app.ui.central.models

data class NotificationItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val message: String,
    var isSeen: Boolean = false,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String = "",
    val data: Map<String, Any> = emptyMap(), // Additional data for specific notification types
    var isPinned: Boolean = false,
    var rsvpStatus: RSVPStatus? = null, // For RSVP notifications
    val eventId: String? = null, // For event-linked notifications
    val venue: String? = null,
    val eventDate: Long? = null,
    val opponent: String? = null,
    val countdownTime: Long? = null // For reminder notifications
)

enum class NotificationType {
    GAME_RSVP,      // Game RSVP request
    GAME_REMINDER,  // Game reminder (countdown)
    PRACTICE_RSVP,  // Practice RSVP request
    PRACTICE_REMINDER, // Practice reminder
    ANNOUNCEMENT,   // Admin announcement
    SCHEDULE_CHANGE, // Schedule change notification
    PLAYER_UPDATE,  // Player joined/left team
    ADMIN_MESSAGE,  // Direct message from admin
    SYSTEM          // System notification
}

enum class RSVPStatus {
    AVAILABLE,   // Green - User is available
    MAYBE,       // Orange - User might be available
    UNAVAILABLE, // Red/Gray - User is not available
    NOT_RESPONDED // No response yet
}

// Extension functions for easier data access
fun NotificationItem.isRSVPNotification(): Boolean {
    return type == NotificationType.GAME_RSVP || type == NotificationType.PRACTICE_RSVP
}

fun NotificationItem.isReminderNotification(): Boolean {
    return type == NotificationType.GAME_REMINDER || type == NotificationType.PRACTICE_REMINDER
}

fun NotificationItem.isActionable(): Boolean {
    return isRSVPNotification() || type == NotificationType.ANNOUNCEMENT
}

fun NotificationItem.getRSVPButtons(): List<RSVPStatus> {
    return if (isRSVPNotification()) {
        listOf(RSVPStatus.AVAILABLE, RSVPStatus.MAYBE, RSVPStatus.UNAVAILABLE)
    } else {
        emptyList()
    }
} 
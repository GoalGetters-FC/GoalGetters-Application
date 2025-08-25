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

    val eventId: String? = null, // For event-linked notifications
    val venue: String? = null,
    val eventDate: Long? = null,
    val opponent: String? = null,
    val countdownTime: Long? = null, // For reminder notifications
    val linkedEventType: LinkedEventType? = null, // What type of event this links to
    val linkedEventId: String? = null, // ID of the linked event
    val teamColor: String? = null, // Team color for accent
    val attendanceCounts: AttendanceCounts? = null // For RSVP notifications
) {
    // Helper methods for notification behavior
    fun isActionable(): Boolean {
        return when (type) {
            NotificationType.GAME_NOTIFICATION, NotificationType.PRACTICE_NOTIFICATION -> true
            NotificationType.GAME_REMINDER, NotificationType.PRACTICE_REMINDER -> true
            NotificationType.ANNOUNCEMENT -> true
            NotificationType.POST_MATCH_SUMMARY -> true
            else -> false
        }
    }
    

    
    fun getLinkedEventTitle(): String {
        return when (linkedEventType) {
            LinkedEventType.GAME -> "Game Details"
            LinkedEventType.PRACTICE -> "Practice Details"
            LinkedEventType.ANNOUNCEMENT -> "Announcement"
            LinkedEventType.MATCH_RESULTS -> "Match Results"
            else -> "Event Details"
        }
    }
}

enum class NotificationType {
    GAME_NOTIFICATION,  // Game-related notification
    GAME_REMINDER,      // Game reminder (countdown)
    PRACTICE_NOTIFICATION, // Practice-related notification
    PRACTICE_REMINDER,  // Practice reminder
    ANNOUNCEMENT,       // Admin announcement
    SCHEDULE_CHANGE,    // Schedule change notification
    PLAYER_UPDATE,      // Player joined/left team
    ADMIN_MESSAGE,      // Direct message from admin
    POST_MATCH_SUMMARY, // Match results and summary
    SYSTEM              // System notification
}

enum class RSVPStatus {
    AVAILABLE,   // Green - User is available
    MAYBE,       // Orange - User might be available
    UNAVAILABLE, // Red/Gray - User is not available
    NOT_RESPONDED // No response yet
}

enum class LinkedEventType {
    GAME,           // Links to game details
    PRACTICE,       // Links to practice details
    ANNOUNCEMENT,   // Links to announcement details
    MATCH_RESULTS   // Links to match results
}

data class AttendanceCounts(
    val available: Int = 0,
    val maybe: Int = 0,
    val unavailable: Int = 0,
    val total: Int = 0
) {
    fun getFormattedSummary(): String {
        return "$available available / $maybe maybe / $unavailable unavailable"
    }
}

// Extension functions for easier data access
fun NotificationItem.isReminderNotification(): Boolean {
    return type == NotificationType.GAME_REMINDER || type == NotificationType.PRACTICE_REMINDER
}

fun NotificationItem.getAccentColor(): String {
    return teamColor ?: "#FFD400" // Default to primary yellow
} 
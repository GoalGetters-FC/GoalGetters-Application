package com.ggetters.app.data.model

import androidx.room.*
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.ggetters.app.data.model.supers.*
import java.time.Instant

@IgnoreExtraProperties
@Entity(
    tableName = "notification",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["team_id"]),
        Index(value = ["type"]),
        Index(value = ["is_seen"]),
        Index(value = ["is_pinned"]),
        Index(value = ["created_at"]),
        Index(value = ["user_id", "is_seen"]),
        Index(value = ["team_id", "type"])
    ]
)
data class Notification(
    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String,

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @Exclude
    @ColumnInfo(name = "stained_at")
    override var stainedAt: Instant? = null,

    // Notification content
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "subtitle")
    val subtitle: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "type")
    val type: NotificationType,

    @ColumnInfo(name = "sender")
    val sender: String = "",

    @ColumnInfo(name = "user_id")
    val userId: String? = null,

    @ColumnInfo(name = "team_id")
    val teamId: String? = null,

    // Notification state
    @ColumnInfo(name = "is_seen")
    var isSeen: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    var isPinned: Boolean = false,

    // Linked event information
    @ColumnInfo(name = "linked_event_type")
    val linkedEventType: LinkedEventType? = null,

    @ColumnInfo(name = "linked_event_id")
    val linkedEventId: String? = null,

    // Additional data (JSON string for Room, Map for Firestore)
    @ColumnInfo(name = "data")
    val data: String = "{}",

    // Notification metadata
    @ColumnInfo(name = "priority")
    val priority: NotificationPriority = NotificationPriority.NORMAL,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Instant? = null,

    @ColumnInfo(name = "action_url")
    val actionUrl: String? = null

) : KeyedEntity, AuditableEntity, StainableEntity {
    companion object { 
        const val TAG = "Notification"
    }

    fun isExpired(): Boolean {
        return expiresAt?.let { it.isBefore(Instant.now()) } ?: false
    }

    fun isActionable(): Boolean {
        return when (type) {
            NotificationType.GAME_NOTIFICATION, 
            NotificationType.PRACTICE_NOTIFICATION,
            NotificationType.GAME_REMINDER, 
            NotificationType.PRACTICE_REMINDER,
            NotificationType.ANNOUNCEMENT,
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

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

// Extension functions for easier data access
fun Notification.isReminderNotification(): Boolean {
    return type == NotificationType.GAME_REMINDER || type == NotificationType.PRACTICE_REMINDER
}

fun Notification.getAccentColor(): String {
    return "#FFD400" // Default to primary yellow
}

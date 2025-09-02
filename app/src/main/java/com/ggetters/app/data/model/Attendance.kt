package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.Exclude
import java.time.Instant

@Entity(
    tableName = "attendance",
    primaryKeys = ["event_id","player_id"],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("player_id")
    ]
)
data class Attendance(
    @ColumnInfo(name = "event_id")   val eventId: String,
    @ColumnInfo(name = "player_id")  val playerId: String,

    @ColumnInfo(name = "status")
    val status: Int,            // 0=Present,1=Absent,2=Late,3=Excused

    @ColumnInfo(name = "recorded_by")
    val recordedBy: String,

    @ColumnInfo(name = "recorded_at")
    val recordedAt: Instant = Instant.now(),

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @Exclude
    @ColumnInfo(name = "stained_at")
    override var stainedAt: Instant? = null

) : AuditableEntity, StainableEntity {
    companion object { const val TAG = "Attendance" }
}

// events
// lineup
// attendance

// stats / performance
// notifications
// auth side, coach has perms to do things, that players cant.
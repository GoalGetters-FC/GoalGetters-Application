package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "performance_log",
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
        Index("event_id"),
        Index("player_id")
    ]
)
data class PerformanceLog(
    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "player_id")
    val playerId: String,

    @ColumnInfo(name = "stat_type")
    val statType: Int,       // 0=Goal,1=Assist,2=YellowCard,3=RedCard,4=SubIn,5=SubOut

    @ColumnInfo(name = "minute_mark")
    val minuteMark: Int? = null,

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

) : KeyedEntity, AuditableEntity, StainableEntity {
    companion object { const val TAG = "PerformanceLog" }
}

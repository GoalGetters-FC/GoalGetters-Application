package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "event",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creator_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["team_id"]),
        Index(value = ["creator_id"])
    ]
)

data class Event(
    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @Exclude
    @ColumnInfo(name = "stained_at")
    override var stainedAt: Instant? = null,

    // — core fields —

    @ColumnInfo(name = "team_id")
    val teamId: String,

    @ColumnInfo(name = "creator_id")
    val creatorId: String? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "category")
    val category: EventCategory,   // PRACTICE, MATCH, OTHER

    @ColumnInfo(name = "style")
    val style: EventStyle,         // STANDARD, FRIENDLY, TOURNAMENT, TRAINING

    @ColumnInfo(name = "start_at")
    val startAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "end_at")
    val endAt: LocalDateTime? = null,

    @ColumnInfo(name = "location")
    val location: String? = null

) : KeyedEntity, AuditableEntity, StainableEntity, Serializable {
    companion object { const val TAG = "Event" }
}

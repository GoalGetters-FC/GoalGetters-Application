package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.ggetters.app.data.model.supers.StashableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "broadcast",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
            onUpdate = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
        ),
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
            onUpdate = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
        ),
//        ForeignKey( // TODO: Link the foreign class when implemented
//            entity = Conference::class,
//            parentColumns = ["id"],
//            childColumns = ["conference_id"],
//            onDelete = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
//            onUpdate = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
//        ),
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["user_id"]),
        Index(value = ["team_id"]),
        Index(value = ["conference_id"]),
    ]
)
data class Broadcast(

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


    @ColumnInfo(name = "stashed_at")
    override var stashedAt: Instant? = null,


    // --- Attributes


    @ColumnInfo(name = "user_id")
    var userId: String,


    @ColumnInfo(name = "team_id")
    var teamId: String,


    @ColumnInfo(name = "conference_id")
    var conferenceId: String?,


    @ColumnInfo(name = "category")
    var category: Int,


    @ColumnInfo(name = "message")
    var message: String,

    
    ) : KeyedEntity, AuditableEntity, StainableEntity, StashableEntity {
    companion object {
        const val TAG = "Broadcast"
    }
}
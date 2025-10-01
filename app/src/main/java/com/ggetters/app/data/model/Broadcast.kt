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
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
        // add Conference FK here
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index("user_id"),             // who sent it
        Index("team_id"),             // team feed lookups
        Index("conference_id"),       // conference scoping
        Index("category"),            // filter by type
        Index("created_at")           // ordering by timestamp
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

    /** mark this broadcast as “read” locally */
    override fun stain() {
        stainedAt = Instant.now()
        updatedAt = Instant.now()
    }

    /** clear the “read” flag */
    fun unstain() {
        stainedAt = null
        updatedAt = Instant.now()
    }

    /** archive locally */
    fun stash() {
        stashedAt = Instant.now()
        updatedAt = Instant.now()
    }

    /** un-archive locally */
    fun unstash() {
        stashedAt = null
        updatedAt = Instant.now()
    }
}
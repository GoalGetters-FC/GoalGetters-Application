package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.ggetters.app.data.model.supers.StashableEntity
import com.google.firebase.firestore.Exclude
import java.time.Instant

/**
 * Composite union to track a users preferences towards a received broadcast.
 *
 * @see Broadcast
 * @see notice
 * @see review
 */
@Entity(
    tableName = "broadcast_status",
    primaryKeys = ["broadcast_id", "recipient_id"],
    indices = [
        Index(value = ["broadcast_id", "recipient_id"], unique = true),
        Index(value = ["broadcast_id"]),
        Index(value = ["recipient_id"]),
    ]
)
data class BroadcastStatus(

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

    @ColumnInfo(name = "broadcast_id")
    val broadcastId: String,

    @ColumnInfo(name = "recipient_id")
    val recipientId: String,

    @ColumnInfo(name = "noticed_at")
    var noticedAt: Instant? = null


) : AuditableEntity, StainableEntity, StashableEntity {
    companion object {
        const val TAG = "BroadcastStatus"
    }


    // --- Functions


    /** mark as “noticed” */
    fun notice() {
        noticedAt = Instant.now()
        updatedAt = Instant.now()
    }

    /** clear the “noticed” flag */
    fun review() {
        noticedAt = null
        updatedAt = Instant.now()
    }

    /** mark this status as “read” locally */
    // override was the suggested fix, not sure why it was needed
    // remove and replace with `override` if needed

    override fun stain() {
        stainedAt = Instant.now()
        updatedAt = Instant.now()
    }

    /** clear the “read” flag */
    fun unstain() {
        stainedAt = null
        updatedAt = Instant.now()
    }

    /** archive this status locally */
    fun stash() {
        stashedAt = Instant.now()
        updatedAt = Instant.now()
    }

    /** un-archive this status */
    fun unstash() {
        stashedAt = null
        updatedAt = Instant.now()
    }
}
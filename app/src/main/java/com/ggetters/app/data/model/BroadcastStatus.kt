package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    indices = [
        Index(value = ["broadcast_id"]),
        Index(value = ["recipient_id"]),
        Index(value = ["broadcast_id", "recipient_id"], unique = true),
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
    // TODO: Figure out how to manage composite keys in Firestore
    
    
    @PrimaryKey // TODO: <-- CKey
    @ColumnInfo(name = "broadcast_id")
    val broadcastId: String,

    
    @PrimaryKey // TODO: <-- CKey
    @ColumnInfo(name = "broadcast_id")
    val recipientId: String,
    
    
    @ColumnInfo(name = "noticed_at")
    var noticedAt: Instant? = null,
    
    
) : AuditableEntity, StainableEntity, StashableEntity {
    companion object {
        const val TAG = "BroadcastStatus"
    }
    
    
    // --- Functions


    /**
     * Stamps [noticedAt] field.
     */
    fun notice() {
        // TODO: Implement similarly to [StainableEntity] + [StashableEntity]
    }
    
    
    /**
     * Clears [noticedAt] field.
     */
    fun review() {
        // TODO: Implement similarly to [StainableEntity] + [StashableEntity]
    }
}
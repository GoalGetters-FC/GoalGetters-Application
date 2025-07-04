package com.ggetters.app.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.models.supers.AuditableEntity
import com.ggetters.app.data.models.supers.CodedEntity
import com.ggetters.app.data.models.supers.KeyedEntity
import com.ggetters.app.data.models.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "team",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["code"], unique = true),
    ]
)
data class Team(
    
    // --- Interfaces

    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @ColumnInfo(name = "stashed_at")
    override var stainedAt: Instant? = null,

    @ColumnInfo(name = "code")
    override var code: String,

    // --- Attributes
    
    @ColumnInfo(name = "name")
    var name: String,
    
) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "Team"
    }
}
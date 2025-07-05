// com/ggetters/app/data/model/Team.kt

package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.ServerTimestamp
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.CodedEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.ggetters.app.data.model.supers.StashableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
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

    
    @ColumnInfo(name = "code")
    override var code: String? = null,

    
    // --- Attributes

    
    @ColumnInfo(name = "name")
    var name: String,

    
    ) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "Team"
    }
}

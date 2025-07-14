package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.core.models.ConfigAppAppearance
import com.ggetters.app.core.models.ConfigAppBroadcasts
import com.ggetters.app.core.models.ConfigSyncFrequency
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "configuration",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["auth_id"], unique = true),
    ]
)
data class Configuration(

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
    

    // --- Attributes


    @ColumnInfo(name = "auth_id")
    var authId: String,


    @ColumnInfo(name = "sync_frequency")
    var syncFrequency: Int = ConfigSyncFrequency.Daily.ordinal,


    @ColumnInfo(name = "app_broadcasts")
    var appBroadcasts: Int = ConfigAppBroadcasts.Accepted.ordinal,


    @ColumnInfo(name = "app_appearance")
    var appAppearance: Int = ConfigAppAppearance.Light.ordinal,


    @ColumnInfo(name = "default_team_id")
    var defaultTeamId: String? = null,


    ) : KeyedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "Configuration"
    }
}
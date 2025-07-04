// com/ggetters/app/data/model/Team.kt

package com.ggetters.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.ServerTimestamp
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StashableEntity
import java.time.Instant
import java.util.UUID

@Entity(tableName = "teams")
@TypeConverters(UuidConverter::class, DateConverter::class)
data class Team(
    @PrimaryKey
    override val id: UUID = UUID.randomUUID(),

    /** Short join-code for this team */
    val code: String = "",

    // --- from AuditableEntity ---
    override val createdAt: Instant = Instant.now(),

    @ServerTimestamp
    override var updatedAt: Instant = Instant.now(),

    // --- from StashableEntity (soft-delete) ---
    override var stashedAt: Instant? = null

) : KeyedEntity, AuditableEntity, StashableEntity

// com/ggetters/app/data/model/User.kt

package com.ggetters.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.ggetters.app.data.model.supers.StashableEntity
import java.time.Instant
import java.util.UUID

/**
 * A single data class for both Room and Firestore.
 */
@Entity(tableName = "users")
@TypeConverters(UuidConverter::class, DateConverter::class)
data class User(
    @PrimaryKey
    override val id: UUID = UUID.randomUUID(),

    /** Optional link to a team */
    val teamId: UUID? = null,

    val authId: String = "",
    val code: String = "",
    val name: String = "",
    val surname: String = "",
    val alias: String? = null,
    val role: Int = 0,
    val gender: String? = null,

    /** When the player was born (from gov ID) */
    val dateOfBirth: Instant = Instant.now(),

    /** When they were “annexed” into the team */
    val annexedAt: Instant? = null,

    // --- from AuditableEntity ---
    override val createdAt: Instant = Instant.now(),

    @ServerTimestamp
    override var updatedAt: Instant = Instant.now(),

    // --- from StashableEntity (soft-delete) ---
    override var stashedAt: Instant? = null,

    // --- from StainableEntity (local-only “dirty” flag) ---
    override var stainedAt: Instant? = null,

    /** UI-only, never persisted */
    @Exclude
    @Transient
    var isSelected: Boolean = false

) : KeyedEntity, AuditableEntity, StashableEntity, StainableEntity

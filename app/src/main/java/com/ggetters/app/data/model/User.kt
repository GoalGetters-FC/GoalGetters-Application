package com.ggetters.app.data.model

import androidx.room.ColumnInfo
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
 * Unified data model for both Room and Firestore.
 * Implements common entity behaviors via supers interfaces.
 */
@Entity(tableName = "users")
@TypeConverters(UuidConverter::class, DateConverter::class)
data class User(
    /**
     * Unique identifier for this user. Generated randomly if unset.
     */
    @PrimaryKey
    @ColumnInfo(name = "id")
    override var id: UUID = UUID.randomUUID(),

    /**
     * Optional link to a Team by UUID.
     */
    @ColumnInfo(name = "team_id")
    var teamId: UUID? = null,

    /**
     * Authentication ID from Firebase Auth.
     */
    var authId: String = "",

    /**
     * Short join-code or shorthand identifier.
     */
    var code: String = "",

    /**
     * User's first name.
     */
    var name: String = "",

    /**
     * User's surname.
     */
    var surname: String = "",

    /**
     * Optional nickname.
     */
    var alias: String? = null,

    /**
     * Numeric role identifier.
     */
    var role: Int = 0,

    /**
     * Optional gender string.
     */
    var gender: String? = null,

    /**
     * When the player was born (from government ID).
     */
    var dateOfBirth: Instant = Instant.now(),

    /**
     * When they were added to the team.
     */
    var annexedAt: Instant? = null,

    // --- from AuditableEntity ---
    override var createdAt: Instant = Instant.now(),

    /**
     * Firestore: server-generated timestamp for updates.
     */
    @ServerTimestamp
    override var updatedAt: Instant = Instant.now(),

    // --- from StashableEntity (soft-delete) ---
    override var stashedAt: Instant? = null,

    // --- from StainableEntity (local 'dirty' flag) ---
    override var stainedAt: Instant? = null,

    /**
     * UI-only flag, excluded from persistence.
     */
    @Exclude
    @Transient
    var isSelected: Boolean = false
) : KeyedEntity, AuditableEntity, StashableEntity, StainableEntity

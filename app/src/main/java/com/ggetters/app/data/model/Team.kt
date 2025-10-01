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
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(
    tableName = "team",
    indices = [
        Index(value = ["code"], unique = true),  // team join codes
        Index(value = ["name"]),                 // team name search
        Index(value = ["is_active"])             // quick active team lookup
    ]
)
data class Team constructor(

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
    var name: String = "",

    @ColumnInfo(name = "alias")
    var alias: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "composition")
    var composition: TeamComposition = TeamComposition.UNISEX_MALE,

    @ColumnInfo(name = "denomination")
    var denomination: TeamDenomination = TeamDenomination.OPEN,

    @ColumnInfo(name = "year_formed")
    var yearFormed: String? = null,

    @ColumnInfo(name = "contact_cell")
    var contactCell: String? = null,

    @ColumnInfo(name = "contact_mail")
    var contactMail: String? = null,

    @ColumnInfo(name = "club_address")
    var clubAddress: String? = null,

    @Exclude
    @ColumnInfo(name = "is_active")
    var isActive: Boolean = false

) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {

    // Firestore needs a real no-arg constructor:
    constructor() : this(
        id            = "",
        createdAt    = Instant.now(),
        updatedAt    = Instant.now(),
        stainedAt     = null,
        code          = null,
        name          = "",
        alias         = null,
        description   = null,
        composition   = TeamComposition.UNISEX_MALE,
        denomination  = TeamDenomination.OPEN,
        yearFormed    = null,
        contactCell   = null,
        contactMail   = null,
        clubAddress   = null,
        isActive      = false
    )

    companion object {
        const val TAG = "Team"
    }
}

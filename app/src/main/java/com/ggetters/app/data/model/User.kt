// app/src/main/java/com/ggetters/app/data/model/User.kt
package com.ggetters.app.data.model

import androidx.room.*
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.ggetters.app.data.model.supers.*
import java.time.Instant
import java.time.LocalDate

@IgnoreExtraProperties
@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["auth_id", "team_id"], unique = true),
        Index(value = ["team_id"])
    ]
)
data class User(
    // 🔑 Use auth UID as the membership doc id inside a team
    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String,            // == authId

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @Exclude
    @ColumnInfo(name = "stained_at")
    override var stainedAt: Instant? = null,

    // ---- attributes ----
    @ColumnInfo(name = "auth_id")
    val authId: String,                 // duplicate of id for clarity/queries

    @ColumnInfo(name = "team_id")
    val teamId: String,

    @ColumnInfo(name = "joined_at")
    var joinedAt: Instant? = null,

    @ColumnInfo(name = "role")
    var role: UserRole = UserRole.FULL_TIME_PLAYER,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "surname")
    var surname: String = "",

    @ColumnInfo(name = "alias")
    var alias: String = "",

    @ColumnInfo(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null, // Room converter; Firestore as ISO string

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "position")
    var position: UserPosition? = null,

    @ColumnInfo(name = "number")
    var number: Int? = null,

    @ColumnInfo(name = "status")
    var status: UserStatus? = UserStatus.ACTIVE,

    @ColumnInfo(name = "health_weight")
    var healthWeight: Double? = null,

    @ColumnInfo(name = "health_height")
    var healthHeight: Double? = null
) : KeyedEntity, AuditableEntity, StainableEntity {
    companion object { const val TAG = "User" }

    fun notifyJoinedTeam() { joinedAt = Instant.now() }
    fun fullName() = "$name $surname".trim()
    fun initials(): String = (name.firstOrNull()?.toString() ?: "") + (surname.firstOrNull()?.toString() ?: "")
}

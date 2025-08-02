package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.CodedEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["code"], unique = true),
        Index(value = ["auth_id", "team_id"], unique = true),
        Index(value = ["auth_id"]),
        Index(value = ["team_id"]),
    ]
)
data class User(

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


    @ColumnInfo(name = "auth_id")
    var authId: String,


    @ColumnInfo(name = "team_id")
    var teamId: String,


    @ColumnInfo(name = "annexed_at")
    var annexedAt: Instant? = null,


    @ColumnInfo(name = "role")
    var role: UserRole,


    @ColumnInfo(name = "name")
    var name: String,


    @ColumnInfo(name = "surname")
    var surname: String,


    @ColumnInfo(name = "alias")
    var alias: String,


    @ColumnInfo(name = "date_of_birth")
    var dateOfBirth: LocalDate,


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
    var healthHeight: Double? = null,


    ) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "User"
    }


    // --- Functions
    
    
    fun NotifyJoinedTeam() {
        annexedAt = Instant.now()
    }


    fun getFullName(): String = "$name $surname"


    fun getInitials(): String {
        val firstInitial = name.firstOrNull()?.toString() ?: ""
        val finalInitial = surname.firstOrNull()?.toString() ?: ""
        return if (firstInitial.isNotEmpty() && finalInitial.isNotEmpty()) {
            "$firstInitial$finalInitial".uppercase()
        } else ""
    }


    fun isAdult(): Boolean = !LocalDate.now().isBefore(dateOfBirth.plusYears(18))
}

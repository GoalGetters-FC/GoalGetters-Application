package com.ggetters.app.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.models.supers.AuditableEntity
import com.ggetters.app.data.models.supers.CodedEntity
import com.ggetters.app.data.models.supers.KeyedEntity
import com.ggetters.app.data.models.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "user",
    foreignKeys = [ForeignKey(
        entity = Team::class,
        parentColumns = ["id"],
        childColumns = ["team_id"],
        onDelete = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
        onUpdate = ForeignKey.CASCADE, // TODO: Confirm expected behaviour
    )],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["code"], unique = true),
        Index(value = ["auth_id"]),
        Index(value = ["team_id"]),
    ]
)
data class User(

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

    @ColumnInfo(name = "auth_id") 
    var authId: String,

    @ColumnInfo(name = "team_id") 
    var teamId: String,

    @ColumnInfo(name = "annexed_at") 
    var annexedAt: Instant? = null,

    @ColumnInfo(name = "role") 
    var role: Int,

    @ColumnInfo(name = "name") 
    var name: String,

    @ColumnInfo(name = "surname") 
    var surname: String,

    @ColumnInfo(name = "alias") 
    var alias: String,

    @ColumnInfo(name = "date_of_birth") 
    var dateOfBirth: Date

) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "User"
    }

    // --- Functions

    fun getFullName(): String = "$name $surname"

    fun getInitials(): String {
        val firstInitial = name.firstOrNull()?.toString() ?: ""
        val finalInitial = surname.firstOrNull()?.toString() ?: ""
        return if (firstInitial.isNotEmpty() && finalInitial.isNotEmpty()) {
            "$firstInitial$finalInitial".uppercase()
        } else ""
    }

    fun isAdult(): Boolean {
        val localizedEighteenthBirthday =
            dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()

        return (!currentDate.isBefore(localizedEighteenthBirthday.plusYears(18)))
    }
}
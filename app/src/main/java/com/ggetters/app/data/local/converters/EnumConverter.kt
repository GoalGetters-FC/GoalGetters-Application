package com.ggetters.app.data.local.converters

import androidx.room.TypeConverter
import com.ggetters.app.data.model.*

/**
 * Type converter for Enum values.
 *
 * Room requires concrete converter methods per enum type.
 * Generic helpers are fine, but they cannot be annotated with @TypeConverter.
 */
class EnumConverter {

    // --- Generic helpers (not annotated) ---
    private fun <T : Enum<T>> fromEnum(value: T?): String? = value?.name
    private inline fun <reified T : Enum<T>> toEnum(value: String?): T? =
        value?.let { enumValueOf<T>(it) }

    // --- UserPosition ---
    @TypeConverter
    fun fromUserPosition(value: UserPosition?): String? = fromEnum(value)

    @TypeConverter
    fun toUserPosition(value: String?): UserPosition? = toEnum<UserPosition>(value)

    // --- UserRole ---
    @TypeConverter
    fun fromUserRole(value: UserRole?): String? = fromEnum(value)

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = toEnum<UserRole>(value)

    // --- UserStatus ---
    @TypeConverter
    fun fromUserStatus(value: UserStatus?): String? = fromEnum(value)

    @TypeConverter
    fun toUserStatus(value: String?): UserStatus? = toEnum<UserStatus>(value)

    // --- TeamComposition ---
    @TypeConverter
    fun fromTeamComposition(value: TeamComposition?): String? = fromEnum(value)

    @TypeConverter
    fun toTeamComposition(value: String?): TeamComposition? = toEnum<TeamComposition>(value)

    // --- TeamDenomination ---
    @TypeConverter
    fun fromTeamDenomination(value: TeamDenomination?): String? = fromEnum(value)

    @TypeConverter
    fun toTeamDenomination(value: String?): TeamDenomination? = toEnum<TeamDenomination>(value)

    // --- EventCategory ---
    @TypeConverter
    fun fromCategory(value: EventCategory?): String? = fromEnum(value)

    @TypeConverter
    fun toCategory(value: String?): EventCategory? = toEnum<EventCategory>(value)

    // --- EventStyle ---
    @TypeConverter
    fun fromStyle(value: EventStyle?): String? = fromEnum(value)

    @TypeConverter
    fun toStyle(value: String?): EventStyle? = toEnum<EventStyle>(value)
}

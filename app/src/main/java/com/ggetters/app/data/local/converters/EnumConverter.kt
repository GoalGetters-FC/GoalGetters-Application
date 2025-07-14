package com.ggetters.app.data.local.converters

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus

/**
 * Type converter for Enum values.
 * 
 * **Note:** The private methods are used to convert any generic Enum into its
 * equivalent string representation. As RoomDB does not support generics and the
 * reified parameter type, each Enum will need its own converter.
 *
 * @see TypeConverter
 * @see RoomDatabase
 */
class EnumConverter {

    
    private fun <T : Enum<T>> fromEnum(value: T?): String? = value?.name
    private inline fun <reified T : Enum<T>> toEnum(value: String?): T? {
        return value?.let { 
            enumValueOf<T>(it) 
        }
    }
    
    
    // --- Implementations of TypeConverters

    
    /**
     * Convert from [UserPosition] to [String].
     */
    @TypeConverter
    fun fromUserPosition(value: UserPosition?): String? = fromEnum(value)


    /**
     * Convert from [String] to [UserPosition].
     */
    @TypeConverter
    fun toUserPosition(value: String?): UserPosition? = toEnum<UserPosition>(value) 


    /**
     * Convert from [UserRole] to [String].
     */
    @TypeConverter
    fun fromUserRole(value: UserRole?): String? = fromEnum(value)


    /**
     * Convert from [String] to [UserRole].
     */
    @TypeConverter
    fun toUserRole(value: String?): UserRole? = toEnum<UserRole>(value)


    /**
     * Convert from [UserStatus] to [String].
     */
    @TypeConverter
    fun fromUserStatus(value: UserStatus?): String? = fromEnum(value)


    /**
     * Convert from [String] to [UserStatus].
     */
    @TypeConverter
    fun toUserStatus(value: String?): UserStatus? = toEnum<UserStatus>(value)


    /**
     * Convert from [TeamComposition] to [String].
     */
    @TypeConverter
    fun fromTeamComposition(value: TeamComposition?): String? = fromEnum(value)


    /**
     * Convert from [String] to [TeamComposition].
     */
    @TypeConverter
    fun toTeamComposition(value: String?): TeamComposition? = toEnum<TeamComposition>(value)


    /**
     * Convert from [TeamDenomination] to [String].
     */
    @TypeConverter
    fun fromTeamDenomination(value: TeamDenomination?): String? = fromEnum(value)


    /**
     * Convert from [String] to [TeamDenomination].
     */
    @TypeConverter
    fun toTeamDenomination(value: String?): TeamDenomination? = toEnum<TeamDenomination>(value)
}
package com.ggetters.app.data.local.converters

import androidx.room.TypeConverter
import java.time.*
import java.util.Date

/**
 * Type converter for [Date] and [Instant] objects to and from [Long], enabling
 * Room to persist time-related fields as primitives.
 *
 * SQLite (used by Room) does not support Java/Kotlin date/time types directly,
 * so Room requires these conversions to store timestamps in INTEGER format.
 *
 * Registered globally via @TypeConverters(...) on the Room database class.
 *
 * @see TypeConverter
 * @see androidx.room.RoomDatabase
 */

/**
 * Type converter for Java/Kotlin date/time types to and from primitives,
 * enabling Room to persist them in SQLite tables.
 *
 * Supports:
 *  - Instant ↔ Long (milliseconds since epoch)
 *  - Date    ↔ Long (milliseconds since epoch)
 *  - LocalDate      ↔ String (ISO-8601 date)
 *  - LocalTime      ↔ String (ISO-8601 time)
 *  - LocalDateTime  ↔ String (ISO-8601 date-time)
 *  - LocalDateTime  ↔ Long (milliseconds since epoch)
 *  - LocalDate      ↔ Long (epoch day)
 *  - LocalTime      ↔ Int  (seconds of day)
 *
 * Registered globally via `@TypeConverters(...)` on the Room database class.
 */
class DateConverter {

    // --- Instant <-> Long (epoch ms) ---

    /**
     * Converts a millisecond timestamp to an [Instant].
     */
    @TypeConverter
    fun epochMsToInstant(ms: Long?): Instant? =
        ms?.let(Instant::ofEpochMilli)

    /**
     * Converts an [Instant] to a millisecond timestamp.
     */
    @TypeConverter
    fun instantToEpochMs(instant: Instant?): Long? =
        instant?.toEpochMilli()

    // --- Date <-> Long (epoch ms) ---

    /**
     * Converts a millisecond timestamp to a [Date].
     */
    @TypeConverter
    fun epochMsToDate(ms: Long?): Date? =
        ms?.let { Date(it) }

    /**
     * Converts a [Date] to a millisecond timestamp.
     */
    @TypeConverter
    fun dateToEpochMs(date: Date?): Long? =
        date?.time

    // --- LocalDate <-> String (ISO) ---

    /**
     * Converts an ISO-8601 date string to [LocalDate].
     */
    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? =
        value?.let(LocalDate::parse)

    /**
     * Converts a [LocalDate] to an ISO-8601 date string.
     */
    @TypeConverter
    fun localDateToString(date: LocalDate?): String? =
        date?.toString()

    // --- LocalTime <-> String (ISO) ---

    /**
     * Converts an ISO-8601 time string to [LocalTime].
     */
    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? =
        value?.let(LocalTime::parse)

    /**
     * Converts a [LocalTime] to an ISO-8601 time string.
     */
    @TypeConverter
    fun localTimeToString(time: LocalTime?): String? =
        time?.toString()

    // --- LocalDateTime <-> String (ISO) ---

    /**
     * Converts an ISO-8601 date-time string to [LocalDateTime].
     */
    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? =
        value?.let(LocalDateTime::parse)

    /**
     * Converts a [LocalDateTime] to an ISO-8601 date-time string.
     */
    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime?): String? =
        dateTime?.toString()

    // --- LocalDateTime <-> Long (epoch ms) ---

    /**
     * Converts a millisecond timestamp to [LocalDateTime] in the system default zone.
     */
    @TypeConverter
    fun epochMsToLocalDateTime(ms: Long?): LocalDateTime? =
        ms?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }

    /**
     * Converts a [LocalDateTime] to a millisecond timestamp in the system default zone.
     */
    @TypeConverter
    fun localDateTimeToEpochMs(dateTime: LocalDateTime?): Long? =
        dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    // --- LocalDate <-> Long (epoch day) ---

    /**
     * Converts a day count since the epoch to [LocalDate].
     */
    @TypeConverter
    fun epochDayToLocalDate(days: Long?): LocalDate? =
        days?.let(LocalDate::ofEpochDay)

    /**
     * Converts a [LocalDate] to a day count since the epoch.
     */
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? =
        date?.toEpochDay()

    // --- LocalTime <-> Int (seconds of day) ---

//    /**
//     * Converts seconds-of-day to [LocalTime].
//     */
//    @TypeConverter
//    fun secondsOfDayToLocalTime(seconds: Int?): LocalTime? =
//        seconds?.let(LocalTime::ofSecondOfDay)

    /**
     * Converts a [LocalTime] to seconds-of-day.
     */
    @TypeConverter
    fun localTimeToSecondsOfDay(time: LocalTime?): Int? =
        time?.toSecondOfDay()

}

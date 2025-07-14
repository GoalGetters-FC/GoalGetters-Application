package com.ggetters.app.core.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


// --- Extensions


/**
 * Extension to convert an [Instant] to a [LocalDate].
 * 
 * @param zoneId the [ZoneId] to use for conversion. If a value is not provided,
 *        the system default will be used to localize the value.
 */
fun Instant.toLocalDate(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDate = atZone(zoneId).toLocalDate()


/**
 * Extension to convert an [Instant] to a [LocalTime].
 * 
 * @param zoneId the [ZoneId] to use for conversion. If a value is not provided,
 *        the system default will be used to localize the value.
 */
fun Instant.toLocalTime(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalTime = atZone(zoneId).toLocalTime()


/**
 * Extension to convert an [Instant] to a [LocalDateTime].
 * 
 * @param zoneId the [ZoneId] to use for conversion. If a value is not provided,
 *        the system default will be used to localize the value.
 */
fun Instant.toLocalDateTime(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDateTime = atZone(zoneId).toLocalDateTime()
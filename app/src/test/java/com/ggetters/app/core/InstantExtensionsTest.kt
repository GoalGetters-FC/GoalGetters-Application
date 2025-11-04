package com.ggetters.app.core

import com.ggetters.app.core.extensions.kotlin.toLocalDate
import com.ggetters.app.core.extensions.kotlin.toLocalDateTime
import com.ggetters.app.core.extensions.kotlin.toLocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class InstantExtensionsTest {
    @Test
    fun instantConversions_respectProvidedZone() {
        val instant = Instant.parse("2024-01-02T03:04:05Z")
        val zone = ZoneId.of("Europe/London")

        val d = instant.toLocalDate(zone)
        val t = instant.toLocalTime(zone)
        val dt = instant.toLocalDateTime(zone)

        assertEquals(2024, d.year)
        assertEquals(1, d.monthValue)
        assertEquals(2, d.dayOfMonth)
        assertEquals(dt.toLocalDate(), d)
        assertEquals(dt.toLocalTime(), t)
    }
}



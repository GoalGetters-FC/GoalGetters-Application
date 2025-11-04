package com.ggetters.app.data.repository.lineup

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory guard to avoid overwriting very recent local edits with remote emissions.
 * Not persisted; only protects the active session from immediate resets.
 */
object LineupLocalEditGuard {
    private val lastEditByEventId: ConcurrentHashMap<String, Instant> = ConcurrentHashMap()

    fun markEdited(eventId: String, at: Instant = Instant.now()) {
        if (eventId.isNotBlank()) {
            lastEditByEventId[eventId] = at
        }
    }

    fun wasRecentlyEdited(eventId: String, windowSeconds: Long = 8L): Boolean {
        val last = lastEditByEventId[eventId] ?: return false
        val elapsed = java.time.Duration.between(last, Instant.now()).seconds
        return elapsed in 0..windowSeconds
    }
}




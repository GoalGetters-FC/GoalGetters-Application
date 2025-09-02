// app/src/main/java/com/ggetters/app/data/remote/firestore/EventFirestore.kt
package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.remote.FirestorePathProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventFirestore @Inject constructor(
    private val paths: FirestorePathProvider
) {

    /** 🔴 Live observer for all events in a team */
    fun observeByTeamId(teamId: String): Flow<List<Event>> = callbackFlow {
        val col = paths.eventsCollection(teamId) // /teams/{teamId}/events
        val sub = col.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents.orEmpty().mapNotNull { it.toEvent(teamId) }
            trySend(list).isSuccess
        }
        awaitClose { sub.remove() }
    }

    /** One-shot fetch all events for a team */
    suspend fun fetchAllForTeam(teamId: String): List<Event> =
        paths.eventsCollection(teamId).get().await().documents.mapNotNull { it.toEvent(teamId) }

    /** One-shot fetch a single event by ID */
    suspend fun getById(teamId: String, eventId: String): Event? =
        paths.eventsCollection(teamId)
            .document(eventId) // ✅ document path (4 segments)
            .get()
            .await()
            .toEvent(teamId)

    /** Create or update an event */
    suspend fun upsert(teamId: String, e: Event) {
        paths.eventsCollection(teamId)
            .document(e.id)
            .set(e.toFirestoreMap())
            .await()
    }

    /** Delete an event */
    suspend fun delete(teamId: String, id: String) {
        paths.eventsCollection(teamId)
            .document(id)
            .delete()
            .await()
    }

    // ---------- Mapping ----------

    private fun DocumentSnapshot.toEvent(teamId: String): Event? {
        val docId = id
        val name = getString("name") ?: return null

        val createdAt = readInstant("createdAt", "created_at") ?: Instant.now()
        val updatedAt = readInstant("updatedAt", "updated_at") ?: createdAt

        val startAt = readLdt("startAt", "start_at") ?: return null
        val endAt = readLdt("endAt", "end_at")

        return Event(
            id = docId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            stainedAt = null,
            teamId = teamId,
            creatorId = getString("creatorId") ?: getString("creator_id"),
            name = name,
            description = getString("description"),
            category = parseCategory(get("category")),
            style = parseStyle(get("style")),
            startAt = startAt,
            endAt = endAt,
            location = getString("location")
        )
    }

    private fun Event.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "teamId" to teamId,
        "creatorId" to creatorId,
        "name" to name,
        "description" to description,
        "category" to category.name,
        "style" to style.name,
        "startAt" to Timestamp(Date.from(startAt.atZone(ZoneId.systemDefault()).toInstant())),
        "endAt" to endAt?.let { Timestamp(Date.from(it.atZone(ZoneId.systemDefault()).toInstant())) },
        "location" to location,
        "createdAt" to Timestamp(Date.from(createdAt)),
        "updatedAt" to Timestamp(Date.from(updatedAt))
    )

    // ---------- Helpers ----------

    private fun DocumentSnapshot.readInstant(vararg keys: String): Instant? {
        for (k in keys) when (val v = get(k)) {
            is Timestamp -> return v.toDate().toInstant()
            is Date      -> return v.toInstant()
            is Number    -> return Instant.ofEpochMilli(v.toLong())
            is String    -> runCatching { Instant.parse(v) }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun DocumentSnapshot.readLdt(vararg keys: String): LocalDateTime? {
        for (k in keys) when (val v = get(k)) {
            is Timestamp -> return LocalDateTime.ofInstant(v.toDate().toInstant(), ZoneId.systemDefault())
            is Date      -> return LocalDateTime.ofInstant(v.toInstant(), ZoneId.systemDefault())
            is Number    -> return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(v.toLong()), ZoneId.systemDefault()
            )
            is String    -> runCatching { LocalDateTime.parse(v) }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun parseCategory(value: Any?): EventCategory =
        when (value) {
            is String -> runCatching { EventCategory.valueOf(value.uppercase()) }.getOrDefault(EventCategory.OTHER)
            is Number -> EventCategory.values().getOrNull(value.toInt()) ?: EventCategory.OTHER
            else      -> EventCategory.OTHER
        }

    private fun parseStyle(value: Any?): EventStyle =
        when (value) {
            is String -> runCatching { EventStyle.valueOf(value.uppercase()) }.getOrDefault(EventStyle.STANDARD)
            is Number -> EventStyle.values().getOrNull(value.toInt()) ?: EventStyle.STANDARD
            else      -> EventStyle.STANDARD
        }
}


/**
 * TODO: Fix Firestore path resolution for Event + Attendance
 *
 * Issue:
 *  - Crash: "Invalid document reference. Document references must have an even number of segments"
 *  - Current code sometimes calls `paths.eventsCollection(teamId)` (collection ref)
 *    and then treats it as a document reference.
 *
 * Fix plan:
 *  1. Ensure `getById(teamId, eventId)` always uses:
 *       paths.eventsCollection(teamId).document(eventId)
 *  2. Remove any leftover `TODO()` placeholders in EventFirestore / AttendanceFirestore.
 *  3. Standardize repo APIs:
 *       - `OnlineEventRepository.getById(id: String)` → resolve teamId internally.
 *       - `OnlineAttendanceRepository` → same pattern.
 *  4. Retest event + attendance creation flow end-to-end.
 */

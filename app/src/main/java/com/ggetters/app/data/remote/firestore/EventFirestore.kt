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

/**
 * Remote Firestore data source for Events.
 *
 * Events are stored in Firestore at:
 *   /teams/{teamId}/events/{eventId}
 *
 * This class handles conversion between Firestore documents and the domain Event model.
 */
@Singleton
class EventFirestore @Inject constructor(
    private val paths: FirestorePathProvider
) {

    /** Observe all events for a given team in real-time. */
    fun observeByTeamId(teamId: String): Flow<List<Event>> = callbackFlow {
        val col = paths.eventsCollection(teamId)
        val sub = col.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val events = snap?.documents.orEmpty().mapNotNull { it.toEvent(teamId) }
            trySend(events).isSuccess
        }
        awaitClose { sub.remove() }
    }

    /** Fetch all events for a given team (one-shot). */
    suspend fun fetchAllForTeam(teamId: String): List<Event> =
        paths.eventsCollection(teamId).get().await()
            .documents.mapNotNull { it.toEvent(teamId) }

    /** Fetch a single event by ID (one-shot). */
    suspend fun getById(teamId: String, eventId: String): Event? =
        paths.eventsCollection(teamId)
            .document(eventId) // ✅ ensures valid path: teams/{teamId}/events/{eventId}
            .get()
            .await()
            .toEvent(teamId)

    /** Create or update an event in Firestore. */
    suspend fun upsert(teamId: String, event: Event) {
        paths.eventsCollection(teamId)
            .document(event.id)
            .set(event.toFirestoreMap())
            .await()
    }

    /** Delete an event from Firestore. */
    suspend fun delete(teamId: String, id: String) {
        paths.eventsCollection(teamId)
            .document(id)
            .delete()
            .await()
    }

    // ---------- Mapping helpers ----------

    private fun DocumentSnapshot.toEvent(teamId: String): Event? {
        val id = this.id
        val name = getString("name") ?: return null

        val createdAt = readInstant("createdAt", "created_at") ?: Instant.now()
        val updatedAt = readInstant("updatedAt", "updated_at") ?: createdAt

        val startAt = readLdt("startAt", "start_at") ?: return null
        val endAt = readLdt("endAt", "end_at")

        return Event(
            id = id,
            teamId = teamId,
            name = name,
            description = getString("description"),
            creatorId = getString("creatorId") ?: getString("creator_id"),
            category = parseCategory(get("category")),
            style = parseStyle(get("style")),
            startAt = startAt,
            endAt = endAt,
            location = getString("location"),
            createdAt = createdAt,
            updatedAt = updatedAt,
            stainedAt = null // never stored in Firestore
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

    // ---------- Value parsers ----------

    private fun DocumentSnapshot.readInstant(vararg keys: String): Instant? =
        keys.asSequence().mapNotNull { key ->
            when (val v = get(key)) {
                is Timestamp -> v.toDate().toInstant()
                is Date      -> v.toInstant()
                is Number    -> Instant.ofEpochMilli(v.toLong())
                is String    -> runCatching { Instant.parse(v) }.getOrNull()
                else         -> null
            }
        }.firstOrNull()

    private fun DocumentSnapshot.readLdt(vararg keys: String): LocalDateTime? =
        keys.asSequence().mapNotNull { key ->
            when (val v = get(key)) {
                is Timestamp -> LocalDateTime.ofInstant(v.toDate().toInstant(), ZoneId.systemDefault())
                is Date      -> LocalDateTime.ofInstant(v.toInstant(), ZoneId.systemDefault())
                is Number    -> LocalDateTime.ofInstant(Instant.ofEpochMilli(v.toLong()), ZoneId.systemDefault())
                is String    -> runCatching { LocalDateTime.parse(v) }.getOrNull()
                else         -> null
            }
        }.firstOrNull()

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

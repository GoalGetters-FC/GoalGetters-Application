package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.model.LineupSpot
import com.ggetters.app.data.model.SpotRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class LineupFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("lineup")

    suspend fun getAll(): List<Lineup> =
        col.get().await().documents.mapNotNull { it.toDomainLineup() }

    suspend fun getByEventId(eventId: String): List<Lineup> =
        col.whereEqualTo("eventId", eventId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toDomainLineup() }

    fun observeByEventId(eventId: String): Flow<List<Lineup>> = callbackFlow {
        val registration = col.whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    com.ggetters.app.core.utils.Clogger.e(
                        "LineupFirestore",
                        "Listener failed for eventId=$eventId: ${error.message}",
                        error
                    )
                    close(error)
                    return@addSnapshotListener
                }

                val lineups = snapshot?.documents.orEmpty().mapNotNull { it.toDomainLineup() }
                trySend(lineups)
            }

        awaitClose { registration.remove() }
    }

    suspend fun getById(id: String): Lineup? =
        col.document(id).get().await().toDomainLineup()

    suspend fun save(lineup: Lineup) {
        col.document(lineup.id).set(lineup.toFirestoreMap()).await()
    }

    suspend fun delete(lineup: Lineup) {
        col.document(lineup.id).delete().await()
    }

    private fun Lineup.toFirestoreMap(): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>(
            "eventId" to eventId,
            "formation" to formation,
            "spots" to spots.map { it.toFirestoreMap() },
            "createdAt" to createdAt.toTimestamp(),
            "updatedAt" to updatedAt.toTimestamp()
        )

        if (!createdBy.isNullOrBlank()) {
            payload["createdBy"] = createdBy
        }

        stainedAt?.let { payload["stainedAt"] = it.toTimestamp() }

        return payload
    }

    private fun LineupSpot.toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "number" to number,
        "position" to position,
        "role" to role.name
    )

    private fun DocumentSnapshot.toDomainLineup(): Lineup? {
        val eventId = getString("eventId") ?: return null
        val formation = getString("formation") ?: return null
        val createdBy = getString("createdBy")
        val spots = get("spots").toLineupSpots()
        val createdAt = readInstant("createdAt") ?: Instant.now()
        val updatedAt = readInstant("updatedAt") ?: createdAt
        val stainedAt = readInstant("stainedAt")

        return Lineup(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            stainedAt = stainedAt,
            eventId = eventId,
            createdBy = createdBy,
            formation = formation,
            spots = spots
        )
    }

    private fun Any?.toLineupSpots(): List<LineupSpot> {
        val raw = this as? List<*> ?: return emptyList()
        return raw.mapNotNull { entry ->
            val data = entry as? Map<*, *> ?: return@mapNotNull null
            val userId = data["userId"] as? String ?: return@mapNotNull null
            val position = data["position"] as? String ?: return@mapNotNull null
            val number = when (val value = data["number"]) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            } ?: 0
            val role = (data["role"] as? String)
                ?.let { runCatching { SpotRole.valueOf(it) }.getOrNull() }
                ?: SpotRole.STARTER

            LineupSpot(
                userId = userId,
                number = number,
                position = position,
                role = role
            )
        }
    }

    private fun DocumentSnapshot.readInstant(field: String): Instant? {
        val value = get(field) ?: return null
        return when (value) {
            is Timestamp -> value.toDate().toInstant()
            is Date -> value.toInstant()
            is Number -> Instant.ofEpochMilli(value.toLong())
            is String -> runCatching { Instant.parse(value) }.getOrNull()
            else -> null
        }
    }

    private fun Instant.toTimestamp(): Timestamp = Timestamp(Date.from(this))
}


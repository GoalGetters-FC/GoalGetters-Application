// app/src/main/java/com/ggetters/app/data/remote/firestore/AttendanceFirestore.kt
package com.ggetters.app.data.remote.firestore

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.remote.FirestorePathProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceFirestore @Inject constructor(
    private val paths: FirestorePathProvider
) {

    private fun col(teamId: String, eventId: String) = run {
        require(teamId.isNotBlank()) { "AttendanceFS: teamId is blank" }
        require(eventId.isNotBlank()) { "AttendanceFS: eventId is blank" }
        paths.attendanceCollection(teamId, eventId)
    }

    // -------- Live observe --------
    fun observeForEvent(teamId: String, eventId: String): Flow<List<Attendance>> = callbackFlow {
        Clogger.i("AttendanceFS", "Observe team=$teamId event=$eventId")
        val sub = col(teamId, eventId).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val rows = snap?.documents?.mapNotNull { it.toAttendance(eventId) }.orEmpty()
            trySend(rows).isSuccess
        }
        awaitClose { sub.remove() }
    }

    // -------- One shots --------
    suspend fun getByEventId(teamId: String, eventId: String): List<Attendance> = try {
        col(teamId, eventId).get().await()
            .documents.mapNotNull { it.toAttendance(eventId) }
    } catch (e: Exception) {
        Clogger.e("AttendanceFS", "getByEventId(team=$teamId, event=$eventId) failed: ${e.message}", e)
        emptyList()
    }

    suspend fun getById(teamId: String, eventId: String, playerId: String): Attendance? = try {
        col(teamId, eventId).document(playerId).get().await().toAttendance(eventId)
    } catch (e: Exception) {
        Clogger.e("AttendanceFS", "getById(team=$teamId, event=$eventId, player=$playerId) failed: ${e.message}", e)
        null
    }

    // -------- Mutations --------
    suspend fun save(teamId: String, row: Attendance) {
        try {
            col(teamId, row.eventId)
                .document(row.playerId)
                .set(row.toFirestoreMap())
                .await()
            Clogger.d("AttendanceFS", "Saved team=$teamId event=${row.eventId} player=${row.playerId}")
        } catch (e: Exception) {
            Clogger.e("AttendanceFS", "save failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun saveAll(teamId: String, rows: List<Attendance>) {
        rows.forEach { save(teamId, it) }
    }

    suspend fun delete(teamId: String, row: Attendance) {
        try {
            col(teamId, row.eventId).document(row.playerId).delete().await()
            Clogger.d("AttendanceFS", "Deleted team=$teamId event=${row.eventId} player=${row.playerId}")
        } catch (e: Exception) {
            Clogger.e("AttendanceFS", "delete failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAllForEvent(teamId: String, eventId: String) {
        try {
            val docs = col(teamId, eventId).get().await().documents
            val db = FirebaseFirestore.getInstance()
            db.runBatch { b -> docs.forEach { b.delete(it.reference) } }.await()
            Clogger.d("AttendanceFS", "Deleted ${docs.size} docs for team=$teamId event=$eventId")
        } catch (e: Exception) {
            Clogger.e("AttendanceFS", "deleteAllForEvent(team=$teamId, event=$eventId) failed: ${e.message}", e)
            throw e
        }
    }

    // -------- Mapping (Firestore <-> Domain) --------
    private fun DocumentSnapshot.toAttendance(eventIdOverride: String): Attendance? {
        val eventId = eventIdOverride
        val playerId = getString("playerId") ?: id
        if (playerId.isBlank()) return null

        val status     = readInt("status") ?: 3
        val recordedBy = readString("recordedBy") ?: "system"
        val recordedAt = readInstant("recordedAt") ?: Instant.now()
        val createdAt  = readInstant("createdAt") ?: recordedAt
        val updatedAt  = readInstant("updatedAt") ?: createdAt
        val notes      = readString("notes")

        return Attendance(
            eventId = eventId,
            playerId = playerId,
            status = status,
            recordedBy = recordedBy,
            recordedAt = recordedAt,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            stainedAt = null
        )
    }

    private fun Attendance.toFirestoreMap(): Map<String, Any?> = mapOf(
        "eventId"    to eventId,
        "playerId"   to playerId,
        "status"     to status,
        "recordedBy" to recordedBy,
        "notes"      to notes,
        "recordedAt" to Timestamp(Date.from(recordedAt)),
        "createdAt"  to Timestamp(Date.from(createdAt)),
        "updatedAt"  to Timestamp(Date.from(updatedAt))
    )

    // -------- Readers (robust) --------
    private fun DocumentSnapshot.readInstant(vararg keys: String): Instant? {
        for (k in keys) {
            when (val v = get(k)) {
                is Timestamp -> return v.toDate().toInstant()
                is Date      -> return v.toInstant()
                is Long      -> return toInstantFromEpoch(v)
                is Int       -> return toInstantFromEpoch(v.toLong())
                is Double    -> return toInstantFromEpoch(v.toLong())
                is String    -> {
                    runCatching { return Instant.parse(v) }.onFailure {
                        v.toLongOrNull()?.let { n -> return toInstantFromEpoch(n) }
                    }
                }
            }
        }
        return null
    }
    private fun toInstantFromEpoch(n: Long): Instant =
        if (n < 10_000_000_000L) Instant.ofEpochSecond(n) else Instant.ofEpochMilli(n)

    private fun DocumentSnapshot.readString(vararg keys: String): String? {
        for (k in keys) {
            getString(k)?.let { return it }
            (get(k) as? Number)?.let { return it.toString() }
        }
        return null
    }

    private fun DocumentSnapshot.readInt(vararg keys: String): Int? {
        for (k in keys) {
            when (val v = get(k)) {
                is Number -> return v.toInt()
                is String -> v.toIntOrNull()?.let { return it }
            }
        }
        return null
    }
}

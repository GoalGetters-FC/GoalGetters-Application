package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.data.remote.FirestorePathProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for [User] entities.
 *
 * Provides real-time streams and suspend functions for CRUD operations
 * against the "users" collection in Firestore.
 */
// app/src/main/java/com/ggetters/app/data/remote/firestore/UserFirestore.kt
@Singleton
class UserFirestore @Inject constructor(
    private val paths: FirestorePathProvider,
    private val db: FirebaseFirestore
) {
    // callers MUST pass teamId explicitly (or inject active team upstream)

    fun observeForTeam(teamId: String): Flow<List<User>> = callbackFlow {
        val col = paths.usersCollection(teamId) // /teams/{teamId}/users
        val sub = col.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents.orEmpty().mapNotNull { it.toUser(teamId) }
            trySend(list).isSuccess
        }
        awaitClose { sub.remove() }
    }

    suspend fun getById(teamId: String, id: String): User? =
        paths.usersCollection(teamId).document(id).get().await().toUser(teamId)

    suspend fun upsert(teamId: String, u: User) {
        paths.usersCollection(teamId).document(u.id).set(u.toFirestoreMap()).await()
    }

    suspend fun delete(teamId: String, id: String) {
        paths.usersCollection(teamId).document(id).delete().await()
    }

    suspend fun fetchAll(teamId: String): List<User> =
        paths.usersCollection(teamId).get().await().documents.mapNotNull { it.toUser(teamId) }

    // ---- mapping helpers ----
    private fun DocumentSnapshot.toUser(teamId: String): User? {
        val id = id
        val authId = getString("authId") ?: id
        val role = getString("role")?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            ?: UserRole.FULL_TIME_PLAYER

        val dobIso = getString("dateOfBirth")
        return User(
            id = id,
            authId = authId,
            teamId = teamId,
            createdAt = readInstant("createdAt") ?: Instant.now(),
            updatedAt = readInstant("updatedAt") ?: Instant.now(),
            joinedAt = readInstant("joinedAt"),
            role = role,
            name = getString("name") ?: "",
            surname = getString("surname") ?: "",
            alias = getString("alias") ?: "",
            dateOfBirth = dobIso?.let(LocalDate::parse),
            email = getString("email"),
            position = getString("position")?.let { runCatching { UserPosition.valueOf(it) }.getOrNull() },
            number = (get("number") as? Number)?.toInt(),
            status = getString("status")?.let { runCatching { UserStatus.valueOf(it) }.getOrNull() } ?: UserStatus.ACTIVE,
            healthWeight = (get("healthWeight") as? Number)?.toDouble(),
            healthHeight = (get("healthHeight") as? Number)?.toDouble()
        )
    }

    private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "authId" to authId,
        "teamId" to teamId,
        "createdAt" to Timestamp(Date.from(createdAt)),
        "updatedAt" to Timestamp(Date.from(updatedAt)),
        "joinedAt" to joinedAt?.let { Timestamp(Date.from(it)) },
        "role" to role.name,
        "name" to name,
        "surname" to surname,
        "alias" to alias,
        "dateOfBirth" to dateOfBirth?.toString(), // ISO
        "email" to email,
        "position" to position?.name,
        "number" to number,
        "status" to status?.name,
        "healthWeight" to healthWeight,
        "healthHeight" to healthHeight
    )

    private fun DocumentSnapshot.readInstant(vararg keys: String): Instant? {
        for (k in keys) when (val v = get(k)) {
            is Timestamp -> return v.toDate().toInstant()
            is Date -> return v.toInstant()
            is Number -> return Instant.ofEpochMilli(v.toLong())
            is String -> runCatching { Instant.parse(v) }.getOrNull()?.let { return it }
        }
        return null
    }

    /** Stream the current user's full name (name + surname) across all teams. */
    fun observeFullNameForAuth(authId: String): Flow<String?> = callbackFlow {
        val q = db.collectionGroup("users")
            .whereEqualTo("authId", authId)
            .limit(1)

        val sub = q.addSnapshotListener { snap, err ->
            if (err != null) {
                // Don't crash the channel; just emit null (fallback will handle it)
                com.ggetters.app.core.utils.Clogger.e("UserFirestore", "FullName listen failed", err)
                trySend(null).isSuccess
                return@addSnapshotListener
            }
            val doc = snap?.documents?.firstOrNull()
            val full = buildFull(doc)
            trySend(full).isSuccess
        }
        awaitClose { sub.remove() }
    }


    /** One-shot fetch of the current user's full name (name + surname). */
    suspend fun fetchFullNameForAuth(authId: String): String? {
        val res = db.collectionGroup("users")
            .whereEqualTo("authId", authId)
            .limit(1)
            .get()
            .await()
        return buildFull(res.documents.firstOrNull())
    }

    private fun buildFull(doc: DocumentSnapshot?): String? {
        val n = doc?.getString("name")?.trim().orEmpty()
        val s = doc?.getString("surname")?.trim().orEmpty()
        val full = listOf(n, s).filter { it.isNotBlank() }.joinToString(" ")
        return full.ifBlank { null }
    }
}


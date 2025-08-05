package com.ggetters.app.data.remote.firestore

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.remote.FirestorePathProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for Team entities.
 *
 * Provides real-time streams and suspend functions for CRUD operations
 * against the "teams" collection in Firestore.
 */
@Singleton
class TeamFirestore @Inject constructor(
    private val paths: FirestorePathProvider
) {
    private val teamsCol = paths.teamCollection()

    fun observeAll(): Flow<List<Team>> = callbackFlow {
        Clogger.i("DevClass", "Observing all teams in Firestore")
        val subscription = teamsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val teams = snapshot?.toObjects(Team::class.java).orEmpty()
                trySend(teams).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getById(id: String): Team? =
        teamsCol.document(id).get().await().toObject(Team::class.java)

    suspend fun getByCode(code: String): Team? {
        val result = teamsCol
            .whereEqualTo("code", code)
            .limit(1)
            .get()
            .await()
        Clogger.i("DevClass", "getByCode: Found ${result.size()} teams with code $code")
        return result.documents.firstOrNull()?.toObject(Team::class.java)
    }

    suspend fun save(team: Team) {
        teamsCol.document(team.id).set(team).await()
        Clogger.i("DevClass", "Saved team: ${team.name} (${team.id})")
    }

    suspend fun delete(id: String) {
        teamsCol.document(id).delete().await()
        Clogger.i("DevClass", "Deleted team with ID: $id")
    }

    suspend fun joinTeam(teamId: String, role: String = "PLAYER") {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No logged-in user")

        val userData = mapOf(
            "role" to role,
            "joinedAt" to Timestamp.now()
        )

        paths.teamCollection()
            .document(teamId)
            .collection("users")
            .document(uid)
            .set(userData)
            .await()
        Clogger.i("DevClass", "User $uid joined team $teamId as $role")
    }
}

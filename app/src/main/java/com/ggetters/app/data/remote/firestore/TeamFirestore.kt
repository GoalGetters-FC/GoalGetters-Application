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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    /**
     * Returns a Flow of teams where the current user (Auth ID) is in the team's users subcollection.
     */
    fun observeTeamsForCurrentUser(): Flow<List<Team>> = callbackFlow {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            trySend(emptyList<Team>()) // Not logged in, show no teams
            close()
            return@callbackFlow
        }

        // Listen to all teams (will be efficient for most real-world club sizes)
        val subscription = teamsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val teams = snapshot?.documents.orEmpty()
                // Now, for each team, check if this user is present in its /users/ subcollection
                // We'll fetch these in parallel, and then send the list when all are ready
                // (or you can optimize by storing a "members" array in each team doc — up to you)
                if (teams.isEmpty()) {
                    trySend(emptyList<Team>())
                } else {
                    // Only teams where user is present in /users/ subcollection
                    kotlinx.coroutines.GlobalScope.launch {
                        val includedTeams = teams.mapNotNull { teamSnap ->
                            val userSnap = teamSnap.reference.collection("users").document(uid).get().await()
                            if (userSnap.exists()) {
                                teamSnap.toObject(Team::class.java)
                            } else null
                        }
                        trySend(includedTeams)
                    }
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    // TeamFirestore.kt

    /** One-shot fetch of all teams you belong to. */
    suspend fun fetchTeamsForCurrentUser(): List<Team> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyList()
        val allTeamsSnap = teamsCol.get().await()
        return allTeamsSnap.documents.mapNotNull { teamDoc ->
            val userSnap = paths.usersCollection(teamDoc.id).document(uid).get().await()
            if (userSnap.exists()) teamDoc.toObject(Team::class.java) else null
        }
    }



}

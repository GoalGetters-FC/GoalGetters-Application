package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Team
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

/**
 * Firestore-backed data source for Team entities.
 *
 * Provides real-time streams and suspend functions for CRUD operations
 * against the "teams" collection in Firestore.
 */
@Singleton
class TeamFirestore(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    // Reference to the "teams" collection
    private val teamsCol = firestore.collection("team")

    /**
     * Observe all teams in real time.
     * Emits the full list whenever any document in the collection changes.
     *
     * @return a Flow emitting the current list of [Team] objects
     */
    fun observeAll(): Flow<List<Team>> = callbackFlow {
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

    /**
     * Fetch a single [Team] by its document ID.
     *
     * @param id the document ID of the team to fetch
     * @return the [Team] object, or null if not found
     */
    suspend fun getById(id: String): Team? =
        teamsCol
            .document(id)
            .get()
            .await()
            .toObject(Team::class.java)

    /**
     * Save or overwrite a [Team] in Firestore.
     * Creates the document if it does not exist.
     *
     * @param team the [Team] object to save
     */
    suspend fun save(team: Team) {
        teamsCol
            .document(team.id)
            .set(team)
            .await()
    }

    /**
     * Delete a team document by its ID.
     *
     * @param id the document ID of the team to delete
     */
    suspend fun delete(id: String) {
        teamsCol
            .document(id)
            .delete()
            .await()
    }
}


/**
 * 🔄 Proposed Firestore Restructure Plan
 *
 * Current Firestore structure is flat (e.g., /users, /events, /lineup),
 * which will become unscalable and harder to secure as data grows.
 *
 * ✅ New structure nests all core data under each team:
 *
 * teams/{teamId}/
 * ├── users/{userId}
 * ├── events/{eventId}
 * │   └── lineups/{lineupId}
 * ├── attendance/{attendanceId}
 * ├── broadcasts/{broadcastId}
 * ├── broadcastStatuses/{statusId}
 * └── metadata/summary (optional)
 *
 * Benefits:
 * - Scoped reads/writes per team
 * - Easier security rules
 * - More efficient syncing and offline support
 * - Prepares for future multi-team support
 *
 * TODO: Refactor all Firestore classes (XFirestore.kt) to use nested paths under /teams/{teamId}
 *       - Update Online repositories to pass teamId
 *       - DevClass should create team first, then seed subcollections
 *       - Optional: Create FirestorePathProvider for safe path construction
 */

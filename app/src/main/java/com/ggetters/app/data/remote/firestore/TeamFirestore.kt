// app/src/main/java/com/ggetters/app/data/remote/firestore/TeamFirestore.kt
package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Team
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
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
    fun observeAllTeams(): Flow<List<Team>> = callbackFlow {
        val subscription = teamsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Close the flow on error
                close(error)
            } else {
                // Send the list of Team objects (or empty list if null)
                val teams = snapshot?.toObjects(Team::class.java).orEmpty()
                trySend(teams).isSuccess
            }
        }
        // Remove the snapshot listener when the flow is closed or cancelled
        awaitClose { subscription.remove() }
    }

    /**
     * Fetch a single [Team] by its document ID.
     *
     * @param id the document ID of the team to fetch
     * @return the [Team] object, or null if not found
     */
    suspend fun fetchTeam(id: String): Team? =
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
    suspend fun saveTeam(team: Team) {
        teamsCol
            .document(team.id.toString())
            .set(team)
            .await()
    }

    /**
     * Delete a team document by its ID.
     *
     * @param id the document ID of the team to delete
     */
    suspend fun deleteTeam(id: String) {
        teamsCol
            .document(id)
            .delete()
            .await()
    }
}

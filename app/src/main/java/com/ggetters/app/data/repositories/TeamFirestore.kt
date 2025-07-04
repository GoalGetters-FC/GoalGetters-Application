package com.ggetters.app.data.repositories

import com.ggetters.app.data.models.Team
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed data source for Team entities.
 */
class TeamFirestore(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val teams = firestore.collection("team")

    suspend fun fetchTeam(id: String): Team =
        teams.document(id).get().await().toObject(Team::class.java) ?: throw NoSuchElementException(
            "Team $id not found"
        )

    suspend fun saveTeam(dto: Team) {
        teams.document(dto.id).set(dto).await()
    }

    fun watchAllTeams(): Flow<List<Team>> = callbackFlow {
        val subscription = teams.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
            } else {
                val list = snap?.documents?.mapNotNull { it.toObject(Team::class.java) }.orEmpty()
                trySend(list).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
}

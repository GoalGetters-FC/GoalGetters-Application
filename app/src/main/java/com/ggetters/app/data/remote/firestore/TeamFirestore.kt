package com.ggetters.app.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.ggetters.app.data.remote.model.TeamDto
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
    private val teams = firestore.collection("teams")

    suspend fun fetchTeam(id: String): TeamDto =
        teams.document(id)
            .get()
            .await()
            .toObject(TeamDto::class.java)
            ?: throw NoSuchElementException("Team $id not found")

    suspend fun saveTeam(dto: TeamDto) {
        teams.document(dto.id)
            .set(dto)
            .await()
    }

    fun watchAllTeams(): Flow<List<TeamDto>> = callbackFlow {
        val subscription = teams.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
            } else {
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(TeamDto::class.java) }
                    .orEmpty()
                trySend(list).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
}

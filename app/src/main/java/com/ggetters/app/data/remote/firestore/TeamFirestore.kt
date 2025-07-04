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

class TeamFirestore(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val teamsCol = firestore.collection("teams")

    fun observeAllTeams(): Flow<List<Team>> = callbackFlow {
        val sub = teamsCol.addSnapshotListener { snap, err ->
            if (err != null) close(err)
            else {
                trySend(snap?.toObjects(Team::class.java).orEmpty()).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    suspend fun fetchTeam(id: String): Team? =
        teamsCol.document(id).get().await().toObject(Team::class.java)

    suspend fun saveTeam(team: Team) {
        teamsCol.document(team.id.toString()).set(team).await()
    }

    suspend fun deleteTeam(id: String) {
        teamsCol.document(id).delete().await()
    }
}

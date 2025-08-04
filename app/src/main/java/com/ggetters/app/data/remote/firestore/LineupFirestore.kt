package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Lineup
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class LineupFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("lineup")

    suspend fun getAll(): List<Lineup> =
        col.get().await().toObjects(Lineup::class.java)

    suspend fun getByEventId(eventId: String): List<Lineup> =
        col.whereEqualTo("eventId", eventId).get().await().toObjects(Lineup::class.java)

    suspend fun getById(id: String): Lineup? =
        col.document(id).get().await().toObject(Lineup::class.java)

    suspend fun save(lineup: Lineup) {
        col.document(lineup.id).set(lineup).await()
    }

    suspend fun delete(lineup: Lineup) {
        col.document(lineup.id).delete().await()
    }
}


package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Broadcast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for [Broadcast] entities.
 *
 * Provides real-time streams and suspend functions for CRUD operations
 * against the "broadcasts" collection in Firestore.
 */
@Singleton
class BroadcastFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Reference to the "broadcasts" collection in Firestore
    private val broadcastsCol = firestore.collection("broadcasts")

    /**
     * Observe all broadcasts in real time.
     * Emits the full list whenever any document in the collection changes.
     */
    fun observeAll(): Flow<List<Broadcast>> = callbackFlow {
        val subscription = broadcastsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val list = snapshot?.toObjects(Broadcast::class.java).orEmpty()
                trySend(list).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Fetch a single [Broadcast] by its document ID.
     *
     * @param id the ID of the broadcast document
     * @return the [Broadcast] object, or null if not found
     */
    suspend fun getById(id: String): Broadcast? =
        broadcastsCol
            .document(id)
            .get()
            .await()
            .toObject(Broadcast::class.java)

    /**
     * Save or overwrite a [Broadcast] in Firestore.
     * Creates the document if it does not exist.
     *
     * @param broadcast the [Broadcast] object to save
     */
    suspend fun save(broadcast: Broadcast) {
        broadcastsCol
            .document(broadcast.id)
            .set(broadcast)
            .await()
    }

    /**
     * Delete a broadcast document by its ID.
     *
     * @param id the ID of the broadcast document to delete
     */
    suspend fun delete(id: String) {
        broadcastsCol
            .document(id)
            .delete()
            .await()
    }
}

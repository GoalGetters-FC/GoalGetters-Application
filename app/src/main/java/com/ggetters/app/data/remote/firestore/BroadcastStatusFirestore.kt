package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.BroadcastStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for [BroadcastStatus] entities.
 *
 * Manages the subcollection "/broadcasts/{broadcastId}/status"
 */
@Singleton
class BroadcastStatusFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun statusCol(broadcastId: String) =
        firestore.collection("broadcasts")
            .document(broadcastId)
            .collection("status")

    /**
     * Observe all statuses for a given broadcast in real time.
     *
     * @param broadcastId the ID of the parent broadcast
     */
    fun observeAll(broadcastId: String): Flow<List<BroadcastStatus>> = callbackFlow {
        val subscription = statusCol(broadcastId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val list = snapshot?.toObjects(BroadcastStatus::class.java).orEmpty()
                    trySend(list).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Fetch a single [BroadcastStatus] by composite key.
     *
     * @param broadcastId the ID of the parent broadcast
     * @param recipientId the ID of the recipient
     */
    suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus? {
        val docId = "${broadcastId}_$recipientId"
        return statusCol(broadcastId)
            .document(docId)
            .get()
            .await()
            .toObject(BroadcastStatus::class.java)
    }

    /**
     * Save or overwrite a [BroadcastStatus] in Firestore.
     * Uses "{broadcastId}_{recipientId}" as the document ID.
     *
     * @param status the [BroadcastStatus] object to save
     */
    suspend fun save(status: BroadcastStatus) {
        val docId = "${status.broadcastId}_${status.recipientId}"
        statusCol(status.broadcastId)
            .document(docId)
            .set(status)
            .await()
    }

    /**
     * Delete a status document by composite key.
     *
     * @param broadcastId the ID of the parent broadcast
     * @param recipientId the ID of the recipient
     */
    suspend fun delete(broadcastId: String, recipientId: String) {
        val docId = "${broadcastId}_$recipientId"
        statusCol(broadcastId)
            .document(docId)
            .delete()
            .await()
    }
}


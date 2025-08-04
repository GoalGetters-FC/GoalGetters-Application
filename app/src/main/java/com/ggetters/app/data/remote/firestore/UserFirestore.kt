package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for [User] entities.
 *
 * Provides real-time streams and suspend functions for CRUD operations
 * against the "users" collection in Firestore.
 */
@Singleton
class UserFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Reference to the "users" collection in Firestore
    private val usersCol = firestore.collection("user")

    /**
     * Observe all users in real time.
     * Emits the full list whenever any document in the collection changes.
     *
     * @return a [Flow] emitting the current list of [User] objects
     */
    fun observeAll(): Flow<List<User>> = callbackFlow {
        val subscription = usersCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Close the flow if an error occurs
                close(error)
            } else {
                // Convert documents to User objects and emit
                val users = snapshot?.toObjects(User::class.java).orEmpty()
                trySend(users).isSuccess
            }
        }
        // Remove the listener when the flow collector is cancelled
        awaitClose { subscription.remove() }
    }

    /**
     * Fetch a single [User] by its document ID.
     *
     * @param id the UUID of the user document
     * @return the [User] object, or null if not found
     */
    suspend fun getById(id: String): User? =
        usersCol
            .document(id.toString())
            .get()
            .await()
            .toObject(User::class.java)

    /**
     * Save or overwrite a [User] in Firestore.
     * Creates the document if it does not exist.
     *
     * @param user the [User] object to save
     */
    suspend fun save(user: User) {
        usersCol
            .document(user.id.toString())
            .set(user)
            .await()
    }

    /**
     * Delete a user document by its UUID.
     *
     * @param id the UUID of the user document to delete
     */
    suspend fun delete(id: String) {
        usersCol
            .document(id.toString())
            .delete()
            .await()
    }

    /*
    Delete all users in the collection.
     */
    suspend fun deleteAll() {
        usersCol
            .get()
            .await()
            .documents
            .forEach { document ->
                document.reference.delete().await()
            }
    }


    /**
     * Fetch a single [User] by their authentication ID.
     *
     * @param authId the authentication ID to search for
     * @return the [User] object, or null if not found
     */
    suspend fun getByAuthId(authId: String): User? =
        usersCol
            .whereEqualTo("authId", authId)
            .limit(1)  // We only need one result
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(User::class.java)
}

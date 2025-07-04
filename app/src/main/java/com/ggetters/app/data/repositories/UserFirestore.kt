package com.ggetters.app.data.repositories

import com.ggetters.app.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed data source for User entities.
 */
class UserFirestore(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val users = firestore.collection("user")

    suspend fun fetchUser(id: String): User =
        users.document(id).get().await().toObject(User::class.java) ?: throw NoSuchElementException(
            "User $id not found"
        )

    suspend fun saveUser(dto: User) {
        users.document(dto.id).set(dto).await()
    }

    fun watchAllUsers(): Flow<List<User>> = callbackFlow {
        val subscription = users.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
            } else {
                val list = snap?.documents?.mapNotNull { it.toObject(User::class.java) }.orEmpty()
                trySend(list).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
}

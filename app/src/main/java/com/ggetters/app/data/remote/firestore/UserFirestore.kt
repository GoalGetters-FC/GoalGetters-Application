package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed data source for User.
 */
@Singleton
class UserFirestore @Inject constructor(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    private val usersCol = firestore.collection("users")

    /** Stream all users from Firestore in real-time */
    fun observeAll(): Flow<List<User>> = callbackFlow {
        val sub = usersCol.addSnapshotListener { snap, err ->
            if (err != null) close(err)
            else {
                val list = snap?.toObjects(User::class.java).orEmpty()
                trySend(list).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    /** One-off fetch by ID */
    suspend fun getById(id: UUID): User? =
        usersCol
            .document(id.toString())
            .get()
            .await()
            .toObject(User::class.java)

    /** Save or overwrite a user in Firestore */
    suspend fun save(user: User) {
        usersCol
            .document(user.id.toString())
            .set(user)
            .await()
    }

    /** Delete a user document */
    suspend fun delete(id: UUID) {
        usersCol
            .document(id.toString())
            .delete()
            .await()
    }
}

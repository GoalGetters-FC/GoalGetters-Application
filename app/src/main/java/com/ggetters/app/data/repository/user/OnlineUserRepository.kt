// app/src/main/java/com/ggetters/app/data/repository/user/OnlineUserRepository.kt
package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.User.Companion.TAG
import com.ggetters.app.data.remote.firestore.UserFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed (online) implementation of [UserRepository].
 *
 * Performs CRUD operations against the remote "users" collection
 * via [UserFirestore]. Does not interact with the local cache.
 */
@Singleton
class OnlineUserRepository @Inject constructor(
    private val firestore: UserFirestore
) : UserRepository {

    /**
     * Observe all users from Firestore in real time.
     *
     * @return a [Flow] emitting the current list of [User] objects whenever
     *         the remote collection is updated.
     */
    override fun observeAll(): Flow<List<User>> =
        firestore.observeAll()

    /**
     * Fetch a single [User] by its UUID from Firestore.
     *
     * @param id the UUID of the user document to retrieve.
     * @return the matching [User], or `null` if not found.
     */
    override suspend fun getById(id: UUID): User? =
        firestore.getById(id)

    /**
     * Save or overwrite a [User] in Firestore.
     *
     * @param user the [User] object to persist remotely.
     */
    override suspend fun save(user: User): Unit =
        firestore.save(user)

    /**
     * Delete a [User] document in Firestore by its UUID.
     *
     * @param id the UUID of the user to delete.
     */
    override suspend fun delete(id: UUID): Unit =
        firestore.delete(id)

    /**
     * Synchronize with remote data.
     *
     * No-op in this implementation, since syncing is handled by [CombinedUserRepository].
     */
    override suspend fun sync(): Unit {
        // Intentionally left blank.
    }

    override suspend fun getLocalByAuthId(authId: String): User? {
        return try {
            // Since this is online repository, we'll fetch from Firestore
            firestore.getByAuthId(authId)
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to get user by auth ID remotely", e)
            null
        }
    }

    override suspend fun insertLocal(user: User) {
        // No-op for online repository
        Clogger.d(TAG, "Skipping local insert in online repository")
    }

    override suspend fun insertRemote(user: User) {
        try {
            firestore.save(user)
            Clogger.d(TAG, "Successfully inserted user remotely: ${user.name}")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to insert user remotely", e)
            throw e
        }
    }
}

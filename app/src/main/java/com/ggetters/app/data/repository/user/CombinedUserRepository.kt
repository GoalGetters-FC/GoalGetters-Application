// app/src/main/java/com/ggetters/app/data/repository/user/CombinedUserRepository.kt
package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates between offline (Room-based) and online (Firestore-based) implementations of [UserRepository].
 *
 * - Reads always from the local cache for instant/offline access.
 * - Writes to local first, then attempts remote writes.
 * - Provides a [sync] method to pull remote data into local storage.
 */
@Singleton
class CombinedUserRepository @Inject constructor(
    private val offline: UserRepository,
    private val online: UserRepository
) : UserRepository {

    /**
     * Observe all users from the local cache.
     *
     * @return a [Flow] that emits the current list of [User] objects whenever the database changes.
     */
    override fun observeAll(): Flow<List<User>> =
        offline.observeAll()

    /**
     * Fetch a single [User] by its UUID from the local cache.
     *
     * @param id the UUID of the user to retrieve.
     * @return the matching [User] or `null` if not found.
     */
    override suspend fun getById(id: UUID): User? =
        offline.getById(id)

    /**
     * Save or update a [User].
     *
     * This writes to the local cache immediately, then attempts to write remotely.
     * Any remote failures are caught and logged but do not interrupt the local write.
     *
     * @param user the [User] object to save.
     */
    override suspend fun save(user: User) {
        // Local write always succeeds
        offline.save(user)

        // Attempt remote write, log on failure
        try {
            online.save(user)
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to save user remotely", e)
        }
    }

    /**
     * Delete a [User] by its UUID.
     *
     * This deletes from the local cache first, then attempts to delete remotely.
     * Any remote failures are caught and logged.
     *
     * @param id the UUID of the user to delete.
     */
    override suspend fun delete(id: UUID) {
        // Local delete
        offline.delete(id)

        // Attempt remote delete, log on failure
        try {
            online.delete(id)
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to delete user remotely", e)
        }
    }

    /**
     * Synchronize local cache with remote data.
     *
     * Fetches all users from the remote source once and upserts them into the local cache.
     * Any errors during fetch or upsert are caught and logged.
     */
    override suspend fun sync() {
        try {
            // Fetch remote snapshot
            val remoteList = online.observeAll().first()
            // Upsert each remote item into local cache
            remoteList.forEach { offline.save(it) }
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to sync users", e)
        }
    }
}

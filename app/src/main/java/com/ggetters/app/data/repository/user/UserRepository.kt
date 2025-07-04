// app/src/main/java/com/ggetters/app/data/repository/user/UserRepository.kt
package com.ggetters.app.data.repository.user

import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Abstraction for loading, saving, deleting, and syncing [User] entities.
 *
 * Implementations may back this interface with a local cache (Room),
 * a remote data source (Firestore), or a combination of both.
 */
interface UserRepository {

    /**
     * Observe all users.
     *
     * @return a [Flow] emitting the current list of all [User] objects whenever
     *         the underlying data source changes.
     */
    fun observeAll(): Flow<List<User>>

    /**
     * Fetch a single [User] by its UUID.
     *
     * @param id the UUID of the user to retrieve.
     * @return the matching [User], or `null` if not found.
     */
    suspend fun getById(id: UUID): User?

    /**
     * Insert or update a [User].
     *
     * If a user with the same UUID already exists, it should be replaced.
     *
     * @param user the [User] object to save.
     */
    suspend fun save(user: User)

    /**
     * Delete a [User] by its UUID.
     *
     * @param id the UUID of the user to delete.
     */
    suspend fun delete(id: UUID)

    /**
     * Synchronize local cache with remote data.
     *
     * For combined implementations, this will pull down all users from the
     * remote data source and upsert them into the local cache.
     */
    suspend fun sync()
}

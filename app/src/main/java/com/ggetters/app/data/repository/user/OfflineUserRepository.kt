// app/src/main/java/com/ggetters/app/data/repository/user/OfflineUserRepository.kt
package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.User.Companion.TAG
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed (offline) implementation of [UserRepository].
 *
 * All operations read from and write to the local SQLite cache via [UserDao].
 * Does not interact with the network or remote data source.
 */
@Singleton
class OfflineUserRepository @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    /**
     * Observe all users from the local database.
     *
     * @return a [Flow] emitting the list of all [User] objects whenever the table changes.
     */
    override fun observeAll(): Flow<List<User>> =
        dao.getAll()

    /**
     * Fetch a single [User] by its UUID from the local database.
     *
     * @param id the UUID of the user to retrieve.
     * @return the matching [User], or `null` if not found.
     */
    override suspend fun getById(id: UUID): User? =
        dao.getByIdOnce(id)

    /**
     * Insert or update a [User] in the local database.
     *
     * On conflict (same primary key), the existing record is replaced.
     *
     * @param user the [User] object to save.
     */
    override suspend fun save(user: User): Unit =
        dao.upsert(user)

    /**
     * Delete a [User] from the local database by its UUID.
     *
     * @param id the UUID of the user to delete.
     */
    override suspend fun delete(id: UUID): Unit =
        dao.deleteById(id)

    /**
     * No-op for offline repository.
     * This method exists to satisfy the [UserRepository] contract.
     */
    override suspend fun sync(): Unit {
        // Intentionally left blank: sync handled by CombinedUserRepository
    }

    override suspend fun getLocalByAuthId(authId: String): User? {
        return try {
            dao.getByAuthId(authId)
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to get user by auth ID", e)
            null
        }
    }

    override suspend fun insertLocal(user: User) {
        try {
            dao.upsert(user)
            Clogger.d(TAG, "Successfully inserted user locally: ${user.name}")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to insert user locally", e)
            throw e
        }
    }

    override suspend fun insertRemote(user: User) {
        // No-op: OfflineRepository doesn't handle remote operations
        Clogger.d(TAG, "Skipping remote insert in offline repository")
    }
}

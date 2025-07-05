// app/src/main/java/com/ggetters/app/data/local/dao/UserDao.kt
package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Data Access Object for the [User] entity.
 *
 * Defines methods for inserting, querying, and deleting user records
 * in the local Room database. Queries either return a [Flow] for
 * reactive updates or suspend functions for one‐off operations.
 */
@Dao
interface UserDao {

    /**
     * Insert or update a single [User].
     * On conflict (same primary key), the existing record is replaced.
     *
     * @param user the [User] to upsert into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    /**
     * Observe all users as a reactive stream.
     * Emits the full list whenever any user row is inserted, updated, or deleted.
     *
     * @return a [Flow] emitting the current list of [User] objects
     */
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    /**
     * Fetch a single [User] by its ID once.
     * This is a one‐time suspendable query that returns null if not found.
     *
     * @param id the unique identifier of the user to fetch
     * @return the matching [User], or null if no user with the given ID exists
     */
    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getByIdOnce(id: UUID): User?

    /**
     * Delete the user with the given ID.
     *
     * @param id the unique identifier of the user to remove
     */
    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: UUID)
}

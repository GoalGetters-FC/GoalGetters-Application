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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<User>)

    @Query("SELECT * FROM user WHERE team_id = :teamId")
    fun getByTeamId(teamId: String): Flow<List<User>>

    @Query("SELECT * FROM user WHERE id = :id AND team_id = :teamId LIMIT 1")
    suspend fun getByIdInTeam(id: String, teamId: String): User?

    @Query("SELECT * FROM user WHERE auth_id = :authId AND team_id = :teamId LIMIT 1")
    suspend fun getByAuthIdAndTeam(authId: String, teamId: String): User?

    @Query("DELETE FROM user WHERE id = :id AND team_id = :teamId")
    suspend fun deleteByIdInTeam(id: String, teamId: String)

    @Query("DELETE FROM user WHERE team_id = :teamId")
    suspend fun deleteAllInTeam(teamId: String)

    // dirty sync helpers
    @Query("SELECT * FROM user WHERE team_id = :teamId AND stained_at IS NOT NULL")
    suspend fun getDirtyUsers(teamId: String): List<User>

    @Query("UPDATE user SET stained_at = NULL WHERE id = :id AND team_id = :teamId")
    suspend fun markClean(id: String, teamId: String)
}

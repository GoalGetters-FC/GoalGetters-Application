package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [Team] entity.
 *
 * Provides methods for inserting, querying, and deleting team records
 * in the local Room database. All operations are suspendable or return
 * a reactive Flow for observing real-time updates.
 */
@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(team: Team)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(teams: List<Team>)

    @Query("SELECT * FROM team WHERE id = :id")
    fun getById(id: String): Flow<Team?>

    @Query("SELECT * FROM team")
    fun getAll(): Flow<List<Team>>

    @Query("DELETE FROM team WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM team")
    suspend fun deleteAll()

    @Query("SELECT * FROM team WHERE is_active = 1 LIMIT 1")
    fun getActiveTeam(): Flow<Team?>

    @Query("SELECT * FROM team WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): Team?

    @Query("UPDATE team SET is_active = 0")
    suspend fun clearActive()

    @Query("UPDATE team SET is_active = 1 WHERE id = :teamId")
    suspend fun setActiveTeam(teamId: String)

    @Query("UPDATE team SET is_active = 0 WHERE id != :teamId")
    suspend fun clearOtherActiveTeams(teamId: String)

    // 1️⃣ new: get only the teams that have been edited locally
    @Query("SELECT * FROM team WHERE stained_at IS NOT NULL")
    fun getDirtyTeams(): Flow<List<Team>>

    // 2️⃣ new: mark a team as clean after it’s been pushed
    @Query("UPDATE team SET stained_at = NULL WHERE id = :teamId")
    suspend fun markClean(teamId: String)

    // ✅ Atomic: ensures exactly one active team
    @Transaction
    suspend fun setActiveAtomic(teamId: String) {
        clearActive()
        setActiveTeam(teamId)
    }

    @Query("UPDATE team SET code = :code, stained_at = :stainedAt, updated_at = :updatedAt WHERE id = :teamId")
    suspend fun updateCode(teamId: String, code: String, stainedAt: java.time.Instant?, updatedAt: java.time.Instant)
}

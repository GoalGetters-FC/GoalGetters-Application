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

    /**
     * Insert or update a single [Team].
     * When a conflict on the primary key occurs, the existing record is replaced.
     *
     * @param team the [Team] object to upsert into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(team: Team)

    /**
     * Insert or update a list of [Team]s in a batch operation.
     * Conflicting records (same primary key) will be replaced.
     *
     * @param teams the list of [Team] objects to upsert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(teams: List<Team>)

    /**
     * Observe a single [Team] by its ID.
     * Returns a [Flow] that emits updates whenever the underlying row changes.
     *
     * @param id the unique identifier of the team to observe
     * @return a [Flow] emitting the matching [Team] or null if not found
     */
    @Query("SELECT * FROM team WHERE id = :id")
    fun getById(id: String): Flow<Team?>

    /**
     * Observe all teams in the database.
     * Returns a [Flow] that emits the full list upon any insert/update/delete.
     *
     * @return a [Flow] emitting the current list of [Team] objects
     */
    @Query("SELECT * FROM team")
    fun getAll(): Flow<List<Team>>

    /**
     * Delete the team with the given ID.
     *
     * @param id the unique identifier of the team to remove
     */
    @Query("DELETE FROM team WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Delete all teams from the database.
     * This operation is irreversible and will remove all records.
     */
    @Query("DELETE FROM team")
    suspend fun deleteAll()
}

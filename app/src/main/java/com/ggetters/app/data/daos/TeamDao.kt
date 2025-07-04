package com.ggetters.app.data.daos

import androidx.room.*
import com.ggetters.app.data.models.Team
import kotlinx.coroutines.flow.Flow

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
}

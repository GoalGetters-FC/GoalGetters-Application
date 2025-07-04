package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(team: Team)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(teams: List<Team>)

    @Query("SELECT * FROM teams WHERE id = :id")
    fun getById(id: String): Flow<Team?>

    @Query("SELECT * FROM teams")
    fun getAll(): Flow<List<Team>>

    @Query("DELETE FROM teams WHERE id = :id")
    suspend fun deleteById(id: String)
}

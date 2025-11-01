package com.ggetters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ggetters.app.data.model.Lineup
import kotlinx.coroutines.flow.Flow

@Dao
interface LineupDao {

    @Query("SELECT * FROM lineup")
    fun getAll(): Flow<List<Lineup>>

    @Query("SELECT * FROM lineup WHERE event_id = :eventId")
    fun getByEventId(eventId: String): Flow<List<Lineup>>

    @Query("SELECT * FROM lineup WHERE id = :id")
    suspend fun getById(id: String): Lineup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(lineup: Lineup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lineups: List<Lineup>)

    @Delete
    suspend fun delete(lineup: Lineup)

    @Query("DELETE FROM lineup")
    suspend fun deleteAll()

    @Query("DELETE FROM lineup WHERE event_id = :eventId")
    suspend fun deleteByEventId(eventId: String)
}

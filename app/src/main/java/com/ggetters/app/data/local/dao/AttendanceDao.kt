package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance")
    fun getAll(): Flow<List<Attendance>>
    @Query("SELECT * FROM attendance WHERE event_id = :eventId")
    fun getByEventId(eventId: String): Flow<List<Attendance>>
    @Query("SELECT * FROM attendance WHERE player_id = :userId")
    fun getByUserId(userId: String): Flow<List<Attendance>>
    @Query("SELECT * FROM attendance WHERE event_id = :eventId AND player_id = :playerId LIMIT 1")
    suspend fun getById(eventId: String, playerId: String): Attendance?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: Attendance)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(entities: List<Attendance>)
    @Delete
    suspend fun delete(entity: Attendance)
    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
    @Query("DELETE FROM attendance WHERE event_id = :eventId")
    suspend fun deleteByEvent(eventId: String)
}

package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    /** Stream everything */
    @Query("SELECT * FROM attendance")
    fun getAll(): Flow<List<Attendance>>

    /** Stream all attendance records for a single event */
    @Query("SELECT * FROM attendance WHERE event_id = :eventId")
    fun getByEventId(eventId: String): Flow<List<Attendance>>

    /** Stream all attendance records for a single player */
    @Query("SELECT * FROM attendance WHERE player_id = :userId")
    fun getByUserId(userId: String): Flow<List<Attendance>>

    /** Lookup one record by its composite key */
    @Query("""
      SELECT * FROM attendance
      WHERE event_id   = :eventId
        AND player_id = :playerId
    """)
    suspend fun getById(eventId: String, playerId: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(attendance: Attendance)

    @Delete
    suspend fun delete(attendance: Attendance)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}

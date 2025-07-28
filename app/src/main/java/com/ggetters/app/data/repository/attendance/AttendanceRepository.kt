// ✅ AttendanceRepository.kt
package com.ggetters.app.data.repository.attendance

import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getAll(): Flow<List<Attendance>>
    fun getByEventId(eventId: String): Flow<List<Attendance>>
    fun getByUserId(userId: String): Flow<List<Attendance>>
    suspend fun getById(eventId: String, playerId: String): Attendance?
    suspend fun upsert(attendance: Attendance)
    suspend fun delete(attendance: Attendance)
    suspend fun deleteAll()
    suspend fun sync()
}

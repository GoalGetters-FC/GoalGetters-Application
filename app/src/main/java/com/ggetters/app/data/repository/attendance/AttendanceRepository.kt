// app/src/main/java/com/ggetters/app/data/repository/attendance/AttendanceRepository.kt
package com.ggetters.app.data.repository.attendance

import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getAll(): Flow<List<Attendance>>
    fun getByEventId(eventId: String): Flow<List<Attendance>>
    fun getByUserId(userId: String): Flow<List<Attendance>>
    suspend fun getById(eventId: String, playerId: String): Attendance?
    suspend fun upsert(attendance: Attendance)
    suspend fun delete(attendance: Attendance)
    suspend fun deleteAll()
    suspend fun deleteAllForEvent(eventId: String)
    suspend fun upsertAll(attendances: List<Attendance>)
    suspend fun sync()
    fun hydrateForTeam(id: String)
}

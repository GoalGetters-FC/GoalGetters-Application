// ✅ CombinedAttendanceRepository.kt
package com.ggetters.app.data.repository.attendance

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedAttendanceRepository @Inject constructor(
    private val offline: OfflineAttendanceRepository,
    private val online: OnlineAttendanceRepository
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = offline.getAll()

    override fun getByEventId(eventId: String): Flow<List<Attendance>> =
        offline.getByEventId(eventId)

    override fun getByUserId(userId: String): Flow<List<Attendance>> =
        offline.getByUserId(userId)

    override suspend fun getById(eventId: String, playerId: String): Attendance? =
        offline.getById(eventId, playerId) ?: online.getById(eventId, playerId)

    override suspend fun upsert(entity: Attendance) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to upsert attendance online: ${e.message}")
        }
    }

    override suspend fun delete(entity: Attendance) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete attendance online: ${e.message}")
        }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
    }

    override suspend fun sync() {
        val remoteList = online.getAll().first()
        remoteList.forEach { offline.upsert(it) }
    }
}
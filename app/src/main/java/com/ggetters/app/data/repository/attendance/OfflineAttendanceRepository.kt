package com.ggetters.app.data.repository.attendance

import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineAttendanceRepository @Inject constructor(
    private val dao: AttendanceDao
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = dao.getAll()
    override fun getByEventId(eventId: String): Flow<List<Attendance>> = dao.getByEventId(eventId)
    override fun getByUserId(userId: String): Flow<List<Attendance>> = dao.getByUserId(userId)

    override suspend fun getById(eventId: String, playerId: String): Attendance? =
        dao.getById(eventId, playerId)

    override suspend fun upsert(entity: Attendance) = dao.save(entity)
    override suspend fun delete(entity: Attendance) = dao.delete(entity)
    override suspend fun deleteAll() = dao.deleteAll()
    override suspend fun deleteAllForEvent(eventId: String) = dao.deleteByEvent(eventId)
    override suspend fun upsertAll(attendances: List<Attendance>) = dao.saveAll(attendances)

    override suspend fun sync() { /* no-op */ }
    override fun hydrateForTeam(id: String) { /* no-op */ }
}

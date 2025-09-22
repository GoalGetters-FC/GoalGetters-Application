package com.ggetters.app.data.repository.attendance

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.model.Attendance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OfflineAttendanceRepository @Inject constructor(
    private val dao: AttendanceDao
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = dao.getAll()
    override fun getByEventId(eventId: String): Flow<List<Attendance>> {
        Clogger.d("OfflineAttendanceRepo", "Getting Flow for eventId: $eventId")
        return dao.getByEventId(eventId).also { flow ->
            // Add logging to see what the flow emits
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                flow.collect { attendances ->
                    Clogger.d("OfflineAttendanceRepo", "Flow emitted: ${attendances.size} attendance records for eventId: $eventId")
                    attendances.forEach { att ->
                        Clogger.d("OfflineAttendanceRepo", "Flow attendance: playerId=${att.playerId}, status=${att.status}")
                    }
                }
            }
        }
    }
    override fun getByUserId(userId: String): Flow<List<Attendance>> = dao.getByUserId(userId)

    override suspend fun getById(eventId: String, playerId: String): Attendance? =
        dao.getById(eventId, playerId)

    override suspend fun upsert(entity: Attendance) {
        Clogger.d("OfflineAttendanceRepo", "Saving attendance: playerId=${entity.playerId}, status=${entity.status}")
        dao.save(entity)
        
        // Verify the data was saved by reading it back
        val saved = dao.getById(entity.eventId, entity.playerId)
        Clogger.d("OfflineAttendanceRepo", "Verification - saved attendance: $saved")
    }
    override suspend fun delete(entity: Attendance) = dao.delete(entity)
    override suspend fun deleteAll() = dao.deleteAll()
    override suspend fun deleteAllForEvent(eventId: String) = dao.deleteByEvent(eventId)
    override suspend fun upsertAll(attendances: List<Attendance>) = dao.saveAll(attendances)

    override suspend fun sync() { /* no-op */ }
    override fun hydrateForTeam(id: String) { /* no-op */ }
}

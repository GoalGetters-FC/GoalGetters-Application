package com.ggetters.app.data.repository.attendance

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.repository.event.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedAttendanceRepository @Inject constructor(
    private val offline: OfflineAttendanceRepository,
    private val online: OnlineAttendanceRepository,
    private val eventRepo: EventRepository
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = offline.getAll()
    override fun getByEventId(eventId: String): Flow<List<Attendance>> = offline.getByEventId(eventId)
    override fun getByUserId(userId: String): Flow<List<Attendance>> = offline.getByUserId(userId)

    override suspend fun getById(eventId: String, playerId: String): Attendance? =
        offline.getById(eventId, playerId) ?: online.getById(eventId, playerId)

    override suspend fun upsert(entity: Attendance) {
        offline.upsert(entity)
        runCatching { online.upsert(entity) }
            .onFailure { e -> Clogger.e("AttendanceRepo", "Online upsert failed: ${e.message}", e) }
    }

    override suspend fun delete(entity: Attendance) {
        offline.delete(entity)
        runCatching { online.delete(entity) }
            .onFailure { e -> Clogger.e("AttendanceRepo", "Online delete failed: ${e.message}", e) }
    }

    override suspend fun deleteAll() = offline.deleteAll()
    override suspend fun deleteAllForEvent(eventId: String) = offline.deleteAllForEvent(eventId)

    override suspend fun upsertAll(attendances: List<Attendance>) {
        offline.upsertAll(attendances)
        runCatching { online.upsertAll(attendances) }
            .onFailure { e -> Clogger.e("AttendanceRepo", "Online bulk upsert failed: ${e.message}", e) }
    }

    /** Replace local attendance with remote for each event in the active team */
    override suspend fun sync() {
        val events = eventRepo.all().first()
        Clogger.i("AttendanceRepo", "Syncing attendance for ${events.size} events")

        events.forEach { event ->
            try {
                val remote = online.getByEventId(event.id).first()
                if (remote.isNotEmpty()) {
                    offline.deleteAllForEvent(event.id)
                    offline.upsertAll(remote)
                }
            } catch (e: Exception) {
                Clogger.e("AttendanceRepo", "Sync failed for event=${event.id}: ${e.message}", e)
            }
        }
    }

    override fun hydrateForTeam(id: String) { /* optional background work */ }
}

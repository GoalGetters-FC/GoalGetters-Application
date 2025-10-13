package com.ggetters.app.data.repository.attendance

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.repository.event.EventRepository
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedAttendanceRepository @Inject constructor(
    private val offline: OfflineAttendanceRepository,
    private val online: OnlineAttendanceRepository,
    private val eventRepo: EventRepository
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = offline.getAll()

    override fun getByEventId(eventId: String): Flow<List<Attendance>> {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_getByEventId")
        trace.start()
        try {
            return offline.getByEventId(eventId)
        } finally {
            trace.stop()
        }
    }

    override fun getByUserId(userId: String): Flow<List<Attendance>> {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_getByUserId")
        trace.start()
        try {
            return offline.getByUserId(userId)
        } finally {
            trace.stop()
        }
    }

    override suspend fun getById(eventId: String, playerId: String): Attendance? {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_getById")
        trace.start()
        try {
            val result = offline.getById(eventId, playerId) ?: online.getById(eventId, playerId)
            if (result != null) trace.putMetric("attendance_found", 1) else trace.putMetric("attendance_found", 0)
            return result
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsert(entity: Attendance) {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_upsert")
        trace.start()
        try {
            offline.upsert(entity)
            runCatching { online.upsert(entity) }
                .onFailure { e -> Clogger.e("AttendanceRepo", "Online upsert failed: ${e.message}", e) }
            trace.putMetric("attendance_upserted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun delete(entity: Attendance) {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_delete")
        trace.start()
        try {
            offline.delete(entity)
            runCatching { online.delete(entity) }
                .onFailure { e -> Clogger.e("AttendanceRepo", "Online delete failed: ${e.message}", e) }
            trace.putMetric("attendance_deleted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun deleteAll() {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_deleteAll")
        trace.start()
        try {
            offline.deleteAll()
            trace.putMetric("attendance_all_deleted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun deleteAllForEvent(eventId: String) {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_deleteAllForEvent")
        trace.start()
        try {
            offline.deleteAllForEvent(eventId)
            trace.putMetric("attendance_deleted_event", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsertAll(attendances: List<Attendance>) {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_upsertAll")
        trace.start()
        try {
            offline.upsertAll(attendances)
            runCatching { online.upsertAll(attendances) }
                .onFailure { e -> Clogger.e("AttendanceRepo", "Online bulk upsert failed: ${e.message}", e) }
            trace.putMetric("attendance_bulk_upserted", attendances.size.toLong())
        } finally {
            trace.stop()
        }
    }

    /** Replace local attendance with remote for each event in the active team */
    override suspend fun sync() {
        val trace = FirebasePerformance.getInstance().newTrace("attendance_sync")
        trace.start()
        try {
            val events = eventRepo.all().first()
            trace.putMetric("events_count", events.size.toLong())
            Clogger.i("AttendanceRepo", "Syncing attendance for ${events.size} events")

            var totalPulled = 0
            events.forEach { event ->
                try {
                    val remote = online.getByEventId(event.id).first()
                    if (remote.isNotEmpty()) {
                        offline.deleteAllForEvent(event.id)
                        offline.upsertAll(remote)
                        totalPulled += remote.size
                    }
                } catch (e: Exception) {
                    Clogger.e("AttendanceRepo", "Sync failed for event=${event.id}: ${e.message}", e)
                }
            }
            trace.putMetric("attendance_pulled", totalPulled.toLong())
        } finally {
            trace.stop()
        }
    }

    override fun hydrateForTeam(id: String) { /* optional background work */ }
}

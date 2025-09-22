package com.ggetters.app.data.repository.attendance

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.remote.firestore.AttendanceFirestore
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OnlineAttendanceRepository @Inject constructor(
    private val firestore: AttendanceFirestore,
    private val teamRepo: TeamRepository,
    private val eventRepo: EventRepository
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = flow { emit(emptyList()) }

    override fun getByEventId(eventId: String): Flow<List<Attendance>> = flow {
        val teamId = resolveTeamIdFor(eventId) ?: return@flow emit(emptyList())
        emit(firestore.getByEventId(teamId, eventId))
    }

    override fun getByUserId(userId: String): Flow<List<Attendance>> = flow {
        // Not supported without eventId context
        emit(emptyList())
    }

    override suspend fun getById(eventId: String, playerId: String): Attendance? {
        val teamId = resolveTeamIdFor(eventId) ?: return null
        return firestore.getById(teamId, eventId, playerId)
    }

    override suspend fun upsert(entity: Attendance) {
        val teamId = resolveTeamIdFor(entity.eventId) ?: return
        firestore.save(teamId, entity)
    }

    override suspend fun delete(entity: Attendance) {
        val teamId = resolveTeamIdFor(entity.eventId) ?: return
        firestore.delete(teamId, entity)
    }

    override suspend fun deleteAll() { /* no global */ }

    override suspend fun deleteAllForEvent(eventId: String) {
        val teamId = resolveTeamIdFor(eventId) ?: return
        firestore.deleteAllForEvent(teamId, eventId)
    }

    override suspend fun upsertAll(attendances: List<Attendance>) {
        if (attendances.isEmpty()) return
        val teamId = resolveTeamIdFor(attendances.first().eventId) ?: return
        firestore.saveAll(teamId, attendances)
    }

    override suspend fun sync() { /* handled in Combined */ }
    override fun hydrateForTeam(id: String) { /* no-op */ }

    private suspend fun resolveTeamIdFor(eventId: String): String? {
        eventRepo.getById(eventId)?.teamId?.let { return it }
        val fallback = teamRepo.getActiveTeam().first()?.id
        if (fallback == null) {
            Clogger.e("OnlineAttendanceRepo", "No teamId for event=$eventId (no active team, event not found).")
        }
        return fallback
    }
}


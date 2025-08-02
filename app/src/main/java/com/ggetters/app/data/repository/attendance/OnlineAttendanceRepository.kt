// ✅ OnlineAttendanceRepository.kt
package com.ggetters.app.data.repository.attendance

import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.remote.firestore.AttendanceFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OnlineAttendanceRepository @Inject constructor(
    private val firestore: AttendanceFirestore
) : AttendanceRepository {

    override fun getAll(): Flow<List<Attendance>> = flow {
        emit(firestore.getAll())
    }

    override fun getByEventId(eventId: String): Flow<List<Attendance>> = flow {
        emit(firestore.getByEventId(eventId))
    }

    override fun getByUserId(userId: String): Flow<List<Attendance>> = flow {
        emit(firestore.getByUserId(userId))
    }

    override suspend fun getById(eventId: String, playerId: String): Attendance? =
        firestore.getById(eventId, playerId)

    override suspend fun upsert(entity: Attendance) = firestore.save(entity)

    override suspend fun delete(entity: Attendance) = firestore.delete(entity)

    override suspend fun deleteAll() {
        // Online-only: no-op
    }
    override suspend fun sync() {
        // Online-only: no-op
    }
}
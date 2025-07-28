// ✅ AttendanceFirestore.kt
package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.Attendance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("attendance")

    suspend fun getAll(): List<Attendance> =
        col.get().await().toObjects(Attendance::class.java)

    suspend fun getByEventId(eventId: String): List<Attendance> =
        col.whereEqualTo("eventId", eventId).get().await().toObjects(Attendance::class.java)

    suspend fun getByUserId(userId: String): List<Attendance> =
        col.whereEqualTo("playerId", userId).get().await().toObjects(Attendance::class.java)

    suspend fun getById(eventId: String, playerId: String): Attendance? =
        col.document("${eventId}_${playerId}").get().await().toObject(Attendance::class.java)

    suspend fun save(attendance: Attendance) {
        col.document("${attendance.eventId}_${attendance.playerId}").set(attendance).await()
    }

    suspend fun delete(attendance: Attendance) {
        col.document("${attendance.eventId}_${attendance.playerId}").delete().await()
    }
}
package com.ggetters.app.data.remote.firestore

import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenByDescending

/**
 * Firestore service for MatchEvent operations.
 * Handles cloud synchronization of match events.
 */
@Singleton
class MatchEventFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val collection = firestore.collection("match_events")
    
    fun getEventsByMatchId(matchId: String): Flow<List<MatchEvent>> = callbackFlow {
        val listenerRegistration = collection
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    com.ggetters.app.core.utils.Clogger.e(
                        "MatchEventFirestore",
                        "Listener failed for matchId=$matchId: ${error.message}",
                        error
                    )
                    return@addSnapshotListener
                }

                val events = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    runCatching {
                        doc.toObject(MatchEvent::class.java)?.copy(id = doc.id)
                    }.getOrNull()
                }.sortedWith(
                    compareByDescending<MatchEvent> { it.minute }
                        .thenByDescending { it.timestamp }
                )

                trySend(events)
            }

        awaitClose { listenerRegistration.remove() }
    }
    
    fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEvent>> = callbackFlow {
        val listenerRegistration = collection
            .whereEqualTo("matchId", matchId)
            .whereEqualTo("eventType", eventType)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    com.ggetters.app.core.utils.Clogger.e(
                        "MatchEventFirestore",
                        "Type listener failed for matchId=$matchId: ${error.message}",
                        error
                    )
                    return@addSnapshotListener
                }

                val events = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    runCatching {
                        doc.toObject(MatchEvent::class.java)?.copy(id = doc.id)
                    }.getOrNull()
                }.sortedWith(
                    compareByDescending<MatchEvent> { it.minute }
                        .thenByDescending { it.timestamp }
                )

                trySend(events)
            }

        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun getEventById(eventId: String): MatchEvent? {
        return try {
            val doc = collection.document(eventId).get().await()
            if (doc.exists()) {
                doc.toObject(MatchEvent::class.java)?.copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun insertEvent(event: MatchEvent) {
        try {
            collection.document(event.id).set(event).await()
        } catch (e: Exception) {
            throw Exception("Failed to insert match event: ${e.message}")
        }
    }
    
    suspend fun updateEvent(event: MatchEvent) {
        try {
            collection.document(event.id).set(event).await()
        } catch (e: Exception) {
            throw Exception("Failed to update match event: ${e.message}")
        }
    }
    
    suspend fun deleteEvent(event: MatchEvent) {
        try {
            collection.document(event.id).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete match event: ${e.message}")
        }
    }
    
    suspend fun deleteEventsByMatchId(matchId: String) {
        try {
            val snapshot = collection
                .whereEqualTo("matchId", matchId)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete events for match: ${e.message}")
        }
    }
    
    suspend fun deleteEventById(eventId: String) {
        try {
            collection.document(eventId).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete match event: ${e.message}")
        }
    }
    
    suspend fun getEventCountByMatchId(matchId: String): Int {
        return try {
            val snapshot = collection
                .whereEqualTo("matchId", matchId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}

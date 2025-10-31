package com.ggetters.app.data.remote.firestore

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFirestore @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "NotificationFirestore"
        private const val COLLECTION_NOTIFICATIONS = "notifications"
        private const val COLLECTION_TEAMS = "teams"
        private const val COLLECTION_USERS = "users"
    }

    /**
     * Send a notification to a specific user
     */
    suspend fun sendToUser(userId: String, notification: Notification): String {
        return try {
            val docRef = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .add(notification)
                .await()
            
            Clogger.d(TAG, "Notification sent to user $userId with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to send notification to user $userId", e)
            throw e
        }
    }

    /**
     * Send a notification to all users in a team
     */
    suspend fun sendToTeam(teamId: String, notification: Notification): List<String> {
        return try {
            // Get all users in the team
            val usersSnapshot = firestore.collection(COLLECTION_TEAMS)
                .document(teamId)
                .collection(COLLECTION_USERS)
                .get()
                .await()

            val notificationIds = mutableListOf<String>()
            
            // Send notification to each user
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val userNotification = notification.copy(
                    userId = userId,
                    teamId = teamId
                )
                
                val docRef = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_NOTIFICATIONS)
                    .add(userNotification)
                    .await()
                
                notificationIds.add(docRef.id)
            }
            
            Clogger.d(TAG, "Notification sent to ${notificationIds.size} users in team $teamId")
            notificationIds
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to send notification to team $teamId", e)
            throw e
        }
    }

    /**
     * Observe notifications for a specific user
     */
    fun observeForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_NOTIFICATIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Clogger.e(TAG, "Error observing notifications for user $userId", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            val notification = doc.toObject(Notification::class.java)
                            notification?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Clogger.e(TAG, "Error parsing notification document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(notifications)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observe unread notifications for a specific user
     */
    fun observeUnreadForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("isSeen", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Clogger.e(TAG, "Error observing unread notifications for user $userId", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            val notification = doc.toObject(Notification::class.java)
                            notification?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Clogger.e(TAG, "Error parsing notification document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(notifications)
                }
            }

        awaitClose { listener.remove() }
    }
    
    /**
     * Observe notifications for a specific team
     */
    fun observeForTeam(teamId: String): Flow<List<Notification>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_TEAMS)
            .document(teamId)
            .collection(COLLECTION_NOTIFICATIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Clogger.e(TAG, "Error observing notifications for team $teamId", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            val notification = doc.toObject(Notification::class.java)
                            notification?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Clogger.e(TAG, "Error parsing notification document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(notifications)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Mark notification as seen
     */
    suspend fun markAsSeen(userId: String, notificationId: String, isSeen: Boolean) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isSeen", isSeen, "updatedAt", Instant.now())
                .await()
            
            Clogger.d(TAG, "Notification $notificationId marked as ${if (isSeen) "seen" else "unseen"}")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to mark notification $notificationId as seen", e)
            throw e
        }
    }

    /**
     * Mark notification as pinned
     */
    suspend fun markAsPinned(userId: String, notificationId: String, isPinned: Boolean) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isPinned", isPinned, "updatedAt", Instant.now())
                .await()
            
            Clogger.d(TAG, "Notification $notificationId marked as ${if (isPinned) "pinned" else "unpinned"}")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to mark notification $notificationId as pinned", e)
            throw e
        }
    }

    /**
     * Delete notification
     */
    suspend fun delete(userId: String, notificationId: String) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .await()
            
            Clogger.d(TAG, "Notification $notificationId deleted")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to delete notification $notificationId", e)
            throw e
        }
    }

    /**
     * Mark all notifications as seen for a user
     */
    suspend fun markAllAsSeen(userId: String) {
        try {
            val batch = firestore.batch()
            val notificationsSnapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("isSeen", false)
                .get()
                .await()

            for (doc in notificationsSnapshot.documents) {
                batch.update(doc.reference, "isSeen", true, "updatedAt", Instant.now())
            }

            batch.commit().await()
            Clogger.d(TAG, "All notifications marked as seen for user $userId")
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to mark all notifications as seen for user $userId", e)
            throw e
        }
    }

    /**
     * Get notification count for a user
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("isSeen", false)
                .get()
                .await()
            
            snapshot.size()
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to get unread count for user $userId", e)
            0
        }
    }
}

package com.ggetters.app.data.repository.notification

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.ggetters.app.data.remote.firestore.NotificationFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class CombinedNotificationRepository @Inject constructor(
    private val offline: OfflineNotificationRepository,
    private val online: OnlineNotificationRepository,
    private val firebaseAuth: FirebaseAuth,
    private val notificationFirestore: NotificationFirestore,
    private val userRepository: com.ggetters.app.data.repository.user.UserRepository
) : NotificationRepository {

    override fun all(): Flow<List<Notification>> {
        Clogger.w("CombinedNotificationRepo", "all() not implemented - use getAllForTeam() instead")
        return offline.all()
    }
    
    override fun getAllForTeam(teamId: String): Flow<List<Notification>> {
        return offline.getAllForTeam(teamId)
    }

    override suspend fun getById(id: String): Notification? {
        return offline.getById(id) ?: online.getById(id)
    }

    override suspend fun upsert(entity: Notification) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to upsert online", e)
        }
    }

    override suspend fun delete(entity: Notification) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to delete online", e)
        }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
        try {
            online.deleteAll()
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to delete all online", e)
        }
    }

    override suspend fun sync() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        try {
            // Get current user's team ID
            val user = userRepository.getLocalByAuthId(currentUserId)
            val teamId = user?.teamId
            
            // Pull notifications from online (both user and team)
            val userNotifications = online.getAllForUser(currentUserId).first()
            val teamNotifications = if (teamId != null) {
                online.getAllForTeam(teamId).first()
            } else {
                emptyList()
            }
            
            // Combine and deduplicate
            val allOnlineNotifications = (userNotifications + teamNotifications)
                .distinctBy { it.id }
            
            // Store in offline (don't delete all, just update)
            allOnlineNotifications.forEach { notification ->
                offline.upsert(notification)
            }
            
            Clogger.d("CombinedNotificationRepo", "Synced ${allOnlineNotifications.size} notifications")
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to sync notifications", e)
        }
    }

    override fun getAllForUser(userId: String): Flow<List<Notification>> {
        return offline.getAllForUser(userId)
    }

    override fun getUnreadForUser(userId: String): Flow<List<Notification>> {
        return offline.getUnreadForUser(userId)
    }

    override fun getPinnedForUser(userId: String): Flow<List<Notification>> {
        return offline.getPinnedForUser(userId)
    }

    override fun getByTypeForUser(userId: String, type: NotificationType): Flow<List<Notification>> {
        return offline.getByTypeForUser(userId, type)
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return offline.getUnreadCount(userId)
    }

    override suspend fun markAsSeen(notificationId: String, isSeen: Boolean) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        offline.markAsSeen(notificationId, isSeen)
        try {
            notificationFirestore.markAsSeen(currentUserId, notificationId, isSeen)
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to mark as seen online", e)
        }
    }

    override suspend fun markAsPinned(notificationId: String, isPinned: Boolean) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        offline.markAsPinned(notificationId, isPinned)
        try {
            notificationFirestore.markAsPinned(currentUserId, notificationId, isPinned)
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to mark as pinned online", e)
        }
    }

    override suspend fun markAllAsSeen(userId: String) {
        offline.markAllAsSeen(userId)
        try {
            online.markAllAsSeen(userId)
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to mark all as seen online", e)
        }
    }

    override suspend fun sendToUser(userId: String, notification: Notification): String {
        // Send online first, then store locally
        return try {
            val notificationId = online.sendToUser(userId, notification)
            val localNotification = notification.copy(id = notificationId, userId = userId)
            offline.upsert(localNotification)
            notificationId
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to send online, storing locally", e)
            offline.sendToUser(userId, notification)
        }
    }

    override suspend fun sendToTeam(teamId: String, notification: Notification): List<String> {
        return try {
            val notificationIds = online.sendToTeam(teamId, notification)
            // Store locally for each notification
            notificationIds.forEach { notificationId ->
                val localNotification = notification.copy(id = notificationId)
                offline.upsert(localNotification)
            }
            notificationIds
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to send to team online", e)
            offline.sendToTeam(teamId, notification)
        }
    }

    override fun searchForUser(userId: String, query: String): Flow<List<Notification>> {
        return offline.searchForUser(userId, query)
    }

    override fun getRecentForUser(userId: String, since: Instant): Flow<List<Notification>> {
        return offline.getRecentForUser(userId, since)
    }

    override suspend fun deleteExpired() {
        offline.deleteExpired()
        try {
            online.deleteExpired()
        } catch (e: Exception) {
            Clogger.e("CombinedNotificationRepo", "Failed to delete expired online", e)
        }
    }
}

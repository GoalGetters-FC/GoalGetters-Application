package com.ggetters.app.data.repository.notification

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.local.dao.NotificationDao
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class OfflineNotificationRepository @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun all(): Flow<List<Notification>> {
        Clogger.w("OfflineNotificationRepo", "all() not implemented - use getAllForTeam() instead")
        return dao.getAllForTeam("")
    }
    
    override fun getAllForTeam(teamId: String): Flow<List<Notification>> {
        Clogger.d("OfflineNotificationRepo", "🔍 Querying notifications for team: $teamId")
        
        // First, let's see ALL notifications in the database
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAll().first().let { allNotifications ->
                Clogger.d("OfflineNotificationRepo", "📊 Total notifications in DB: ${allNotifications.size}")
                allNotifications.forEach { notification ->
                    Clogger.d("OfflineNotificationRepo", "  ├─ ${notification.id.take(8)}: '${notification.title}' | userId=${notification.userId?.take(8)} | teamId=${notification.teamId?.take(8)}")
                }
            }
        }
        
        return dao.getAllForTeam(teamId).map { notifications ->
            Clogger.d("OfflineNotificationRepo", "✅ DAO returned ${notifications.size} notifications for team $teamId")
            notifications.forEach { notification ->
                Clogger.d("OfflineNotificationRepo", "  ├─ ${notification.title} (teamId: ${notification.teamId}, userId: ${notification.userId})")
            }
            notifications
        }
    }

    override suspend fun getById(id: String): Notification? {
        return dao.getById(id)
    }

    override suspend fun upsert(entity: Notification) {
        Clogger.d("OfflineNotificationRepo", "📥 Upserting notification:")
        Clogger.d("OfflineNotificationRepo", "  ├─ ID: ${entity.id}")
        Clogger.d("OfflineNotificationRepo", "  ├─ Title: ${entity.title}")
        Clogger.d("OfflineNotificationRepo", "  ├─ userId: ${entity.userId}")
        Clogger.d("OfflineNotificationRepo", "  └─ teamId: ${entity.teamId}")
        
        try {
            dao.insert(entity)
            Clogger.d("OfflineNotificationRepo", "✅ Notification inserted to DAO")
            
            // Verify it was actually inserted
            val inserted = dao.getById(entity.id)
            if (inserted != null) {
                Clogger.d("OfflineNotificationRepo", "✅ VERIFIED: Found in DB (userId: ${inserted.userId}, teamId: ${inserted.teamId})")
            } else {
                Clogger.e("OfflineNotificationRepo", "❌ ERROR: NOT FOUND in DB immediately after insert!")
            }
        } catch (e: Exception) {
            Clogger.e("OfflineNotificationRepo", "❌ Failed to insert notification: ${e.message}", e)
            throw e
        }
    }

    override suspend fun delete(entity: Notification) {
        dao.delete(entity)
    }

    override suspend fun deleteAll() {
        Clogger.w("OfflineNotificationRepo", "deleteAll() not implemented - use deleteAllForUser() instead")
    }

    override suspend fun sync() {
        // No-op for offline repository
    }

    override fun getAllForUser(userId: String): Flow<List<Notification>> {
        Clogger.d("OfflineNotificationRepo", "Querying notifications for user: $userId")
        return dao.getAllForUser(userId).map { notifications ->
            Clogger.d("OfflineNotificationRepo", "DAO returned ${notifications.size} notifications for user $userId")
            if (notifications.isEmpty()) {
                Clogger.w("OfflineNotificationRepo", "No notifications found for user $userId - this might indicate a database issue")
            } else {
                notifications.forEach { notification ->
                    Clogger.d("OfflineNotificationRepo", "Found notification: ${notification.id} - ${notification.title} (userId: ${notification.userId})")
                }
            }
            notifications
        }
    }

    override fun getUnreadForUser(userId: String): Flow<List<Notification>> {
        return dao.getUnreadForUser(userId)
    }

    override fun getPinnedForUser(userId: String): Flow<List<Notification>> {
        return dao.getPinnedForUser(userId)
    }

    override fun getByTypeForUser(userId: String, type: NotificationType): Flow<List<Notification>> {
        return dao.getByTypeForUser(userId, type)
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return dao.getUnreadCount(userId)
    }

    override suspend fun markAsSeen(notificationId: String, isSeen: Boolean) {
        dao.markAsSeen(notificationId, isSeen)
    }

    override suspend fun markAsPinned(notificationId: String, isPinned: Boolean) {
        dao.markAsPinned(notificationId, isPinned)
    }

    override suspend fun markAllAsSeen(userId: String) {
        dao.markAllAsSeen(userId)
    }

    override suspend fun sendToUser(userId: String, notification: Notification): String {
        // For offline, just insert locally
        val localNotification = notification.copy(userId = userId)
        dao.insert(localNotification)
        return localNotification.id
    }

    override suspend fun sendToTeam(teamId: String, notification: Notification): List<String> {
        // For offline, this would need team member lookup
        // For now, just return empty list
        Clogger.w("OfflineNotificationRepo", "sendToTeam() not fully implemented for offline mode")
        return emptyList()
    }

    override fun searchForUser(userId: String, query: String): Flow<List<Notification>> {
        return dao.searchForUser(userId, query)
    }

    override fun getRecentForUser(userId: String, since: Instant): Flow<List<Notification>> {
        return dao.getRecentForUser(userId, since)
    }

    override suspend fun deleteExpired() {
        dao.deleteExpired()
    }
}

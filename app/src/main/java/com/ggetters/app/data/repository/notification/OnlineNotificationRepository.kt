package com.ggetters.app.data.repository.notification

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.remote.firestore.NotificationFirestore
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class OnlineNotificationRepository @Inject constructor(
    private val firestore: NotificationFirestore
) : NotificationRepository {

    override fun all(): Flow<List<Notification>> {
        Clogger.w("OnlineNotificationRepo", "all() not implemented - use getAllForTeam() instead")
        return firestore.observeForTeam("")
    }
    
    override fun getAllForTeam(teamId: String): Flow<List<Notification>> {
        return firestore.observeForTeam(teamId)
    }

    override suspend fun getById(id: String): Notification? {
        Clogger.w("OnlineNotificationRepo", "getById() not implemented for online repository")
        return null
    }

    override suspend fun upsert(entity: Notification) {
        Clogger.w("OnlineNotificationRepo", "upsert() not implemented - use sendToUser() instead")
    }

    override suspend fun delete(entity: Notification) {
        entity.userId?.let { userId ->
            firestore.delete(userId, entity.id)
        } ?: run {
            Clogger.w("OnlineNotificationRepo", "Cannot delete notification without userId")
        }
    }

    override suspend fun deleteAll() {
        Clogger.w("OnlineNotificationRepo", "deleteAll() not implemented for online repository")
    }

    override suspend fun sync() {
        // No-op for online repository
    }

    override fun getAllForUser(userId: String): Flow<List<Notification>> {
        return firestore.observeForUser(userId)
    }

    override fun getUnreadForUser(userId: String): Flow<List<Notification>> {
        return firestore.observeUnreadForUser(userId)
    }

    override fun getPinnedForUser(userId: String): Flow<List<Notification>> {
        // Firestore doesn't have a direct query for pinned notifications
        // We'll filter in the combined repository
        return firestore.observeForUser(userId)
    }

    override fun getByTypeForUser(userId: String, type: NotificationType): Flow<List<Notification>> {
        // Firestore doesn't have a direct query for type filtering
        // We'll filter in the combined repository
        return firestore.observeForUser(userId)
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return firestore.getUnreadCount(userId)
    }

    override suspend fun markAsSeen(notificationId: String, isSeen: Boolean) {
        // This needs userId, but we don't have it in the interface
        // We'll handle this in the combined repository
        Clogger.w("OnlineNotificationRepo", "markAsSeen() needs userId - handled in combined repository")
    }

    override suspend fun markAsPinned(notificationId: String, isPinned: Boolean) {
        // This needs userId, but we don't have it in the interface
        // We'll handle this in the combined repository
        Clogger.w("OnlineNotificationRepo", "markAsPinned() needs userId - handled in combined repository")
    }

    override suspend fun markAllAsSeen(userId: String) {
        firestore.markAllAsSeen(userId)
    }

    override suspend fun sendToUser(userId: String, notification: Notification): String {
        return firestore.sendToUser(userId, notification)
    }

    override suspend fun sendToTeam(teamId: String, notification: Notification): List<String> {
        return firestore.sendToTeam(teamId, notification)
    }

    override fun searchForUser(userId: String, query: String): Flow<List<Notification>> {
        // Firestore doesn't have full-text search
        // We'll filter in the combined repository
        return firestore.observeForUser(userId)
    }

    override fun getRecentForUser(userId: String, since: Instant): Flow<List<Notification>> {
        // Firestore doesn't have a direct query for recent notifications
        // We'll filter in the combined repository
        return firestore.observeForUser(userId)
    }

    override suspend fun deleteExpired() {
        // Firestore doesn't have a direct query for expired notifications
        // We'll handle this in the combined repository
        Clogger.w("OnlineNotificationRepo", "deleteExpired() not implemented for online repository")
    }
}

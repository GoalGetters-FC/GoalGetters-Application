package com.ggetters.app.data.repository.notification

import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface NotificationRepository {
    
    /**
     * Get all notifications
     */
    fun all(): Flow<List<Notification>>
    
    /**
     * Get notification by ID
     */
    suspend fun getById(id: String): Notification?
    
    /**
     * Insert or update notification
     */
    suspend fun upsert(entity: Notification)
    
    /**
     * Delete notification
     */
    suspend fun delete(entity: Notification)
    
    /**
     * Delete all notifications
     */
    suspend fun deleteAll()
    
    /**
     * Sync notifications
     */
    suspend fun sync()
    
    /**
     * Get all notifications for a specific user
     */
    fun getAllForUser(userId: String): Flow<List<Notification>>
    
    /**
     * Get unread notifications for a specific user
     */
    fun getUnreadForUser(userId: String): Flow<List<Notification>>
    
    /**
     * Get pinned notifications for a specific user
     */
    fun getPinnedForUser(userId: String): Flow<List<Notification>>
    
    /**
     * Get notifications by type for a specific user
     */
    fun getByTypeForUser(userId: String, type: NotificationType): Flow<List<Notification>>
    
    /**
     * Get unread notification count for a user
     */
    suspend fun getUnreadCount(userId: String): Int
    
    /**
     * Mark notification as seen/unseen
     */
    suspend fun markAsSeen(notificationId: String, isSeen: Boolean)
    
    /**
     * Mark notification as pinned/unpinned
     */
    suspend fun markAsPinned(notificationId: String, isPinned: Boolean)
    
    /**
     * Mark all notifications as seen for a user
     */
    suspend fun markAllAsSeen(userId: String)
    
    /**
     * Send notification to a specific user
     */
    suspend fun sendToUser(userId: String, notification: Notification): String
    
    /**
     * Send notification to all users in a team
     */
    suspend fun sendToTeam(teamId: String, notification: Notification): List<String>
    
    /**
     * Search notifications for a user
     */
    fun searchForUser(userId: String, query: String): Flow<List<Notification>>
    
    /**
     * Get recent notifications for a user
     */
    fun getRecentForUser(userId: String, since: Instant): Flow<List<Notification>>
    
    /**
     * Delete expired notifications
     */
    suspend fun deleteExpired()
}

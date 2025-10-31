package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notification WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllForUser(userId: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE user_id = :userId AND is_seen = 0 ORDER BY created_at DESC")
    fun getUnreadForUser(userId: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE user_id = :userId AND is_pinned = 1 ORDER BY created_at DESC")
    fun getPinnedForUser(userId: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE team_id = :teamId ORDER BY created_at DESC")
    fun getAllForTeam(teamId: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification ORDER BY created_at DESC")
    fun getAll(): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE user_id = :userId AND type = :type ORDER BY created_at DESC")
    fun getByTypeForUser(userId: String, type: NotificationType): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE id = :id")
    suspend fun getById(id: String): Notification?
    
    @Query("SELECT COUNT(*) FROM notification WHERE user_id = :userId AND is_seen = 0")
    suspend fun getUnreadCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<Notification>)
    
    @Update
    suspend fun update(notification: Notification)
    
    @Query("UPDATE notification SET is_seen = :isSeen WHERE id = :id")
    suspend fun markAsSeen(id: String, isSeen: Boolean)
    
    @Query("UPDATE notification SET is_pinned = :isPinned WHERE id = :id")
    suspend fun markAsPinned(id: String, isPinned: Boolean)
    
    @Query("UPDATE notification SET is_seen = 1 WHERE user_id = :userId")
    suspend fun markAllAsSeen(userId: String)
    
    @Delete
    suspend fun delete(notification: Notification)
    
    @Query("DELETE FROM notification WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM notification WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
    
    @Query("DELETE FROM notification WHERE team_id = :teamId")
    suspend fun deleteAllForTeam(teamId: String)
    
    @Query("DELETE FROM notification WHERE expires_at < :now")
    suspend fun deleteExpired(now: Instant = Instant.now())
    
    @Query("SELECT * FROM notification WHERE user_id = :userId AND (title LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%') ORDER BY created_at DESC")
    fun searchForUser(userId: String, query: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notification WHERE user_id = :userId AND created_at >= :since ORDER BY created_at DESC")
    fun getRecentForUser(userId: String, since: Instant): Flow<List<Notification>>
}

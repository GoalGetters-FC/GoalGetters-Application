package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.LocalNotificationService
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.repository.notification.NotificationRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val localNotificationService: LocalNotificationService,
    private val firebaseAuth: FirebaseAuth,
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "NotificationsViewModel"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // Get current user ID (local user ID, not Firebase Auth ID)
    private val currentUserId: StateFlow<String?> = kotlinx.coroutines.flow.flow {
        val authId = firebaseAuth.currentUser?.uid
        if (authId != null) {
            val localUser = userRepository.getLocalByAuthId(authId)
            emit(localUser?.id)
        } else {
            emit(null)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Get all notifications for current team (more reliable than user-based)
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Get unread notifications (filtered from team notifications)
    val unreadNotifications: StateFlow<List<Notification>> = combine(
        notifications,
        _isLoading
    ) { allNotifications, loading ->
        if (loading) emptyList() else {
            allNotifications.filter { !it.isSeen }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Get pinned notifications (filtered from team notifications)
    val pinnedNotifications: StateFlow<List<Notification>> = combine(
        notifications,
        _isLoading
    ) { allNotifications, loading ->
        if (loading) emptyList() else {
            allNotifications.filter { it.isPinned }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        // Start EventBus observer FIRST to catch any notifications emitted during initialization
        observeNotificationEvents()
        loadNotifications()
        observeUnreadCount()
        observeRealTimeUpdates()
    }

    // No need for FCM topic subscription with local notifications

    /**
     * Load notifications for the current user
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Get active team
                val team = teamRepository.getActiveTeam().stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                ).value
                
                Clogger.d(TAG, "Loading notifications for team: ${team?.id}")
                
                if (team != null) {
                    // Load notifications for the team
                    val teamNotifications = notificationRepository.getAllForTeam(team.id).stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5000),
                        emptyList()
                    ).value
                    
                    // Also load user-specific notifications (FCM notifications)
                    val currentUserId = firebaseAuth.currentUser?.uid
                    val userNotifications = if (currentUserId != null) {
                        notificationRepository.getAllForUser(currentUserId).stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5000),
                            emptyList()
                        ).value
                    } else {
                        emptyList()
                    }
                    
                    // Combine team and user notifications, remove duplicates
                    val allNotifications = (teamNotifications + userNotifications)
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    
                    Clogger.d(TAG, "Found ${allNotifications.size} notifications (${teamNotifications.size} team + ${userNotifications.size} user)")
                    allNotifications.forEach { notification ->
                        Clogger.d(TAG, "Notification: ${notification.title} - ${notification.message} (Team: ${notification.teamId}, User: ${notification.userId})")
                    }
                    
                    // Merge with existing EventBus notifications to avoid overriding them
                    val currentNotifications = _notifications.value.toMutableList()
                    val mergedNotifications = (currentNotifications + allNotifications)
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    
                    Clogger.d(TAG, "Merged notifications: ${mergedNotifications.size} total (${currentNotifications.size} existing + ${allNotifications.size} loaded)")
                    _notifications.value = mergedNotifications
                } else {
                    Clogger.w(TAG, "No active team found, loading user notifications only")
                    
                    // Load user-specific notifications even without a team
                    val currentUserId = firebaseAuth.currentUser?.uid
                    Clogger.d(TAG, "Loading user notifications for userId: $currentUserId")
                    val userNotifications = if (currentUserId != null) {
                        notificationRepository.getAllForUser(currentUserId).stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5000),
                            emptyList()
                        ).value
                    } else {
                        emptyList()
                    }
                    
                    Clogger.d(TAG, "Found ${userNotifications.size} user notifications")
                    userNotifications.forEach { notification ->
                        Clogger.d(TAG, "User notification: ${notification.title} - ${notification.message} (userId: ${notification.userId}, teamId: ${notification.teamId})")
                    }
                    
                    // Debug: Check if we can find the notification directly by ID
                    if (userNotifications.isEmpty()) {
                        Clogger.w(TAG, "No user notifications found - checking database directly")
                        // Try to find any notification with this userId
                        try {
                            val directQuery = notificationRepository.getById("a0f4dbf4-45e4-4276-9eca-a44e9367a505") // From the logs
                            Clogger.d(TAG, "Direct query result: ${directQuery?.title ?: "null"}")
                        } catch (e: Exception) {
                            Clogger.e(TAG, "Direct query failed", e)
                        }
                    }
                    
                    // Merge with existing EventBus notifications to avoid overriding them
                    val currentNotifications = _notifications.value.toMutableList()
                    val mergedNotifications = (currentNotifications + userNotifications)
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    
                    Clogger.d(TAG, "Merged user notifications: ${mergedNotifications.size} total (${currentNotifications.size} existing + ${userNotifications.size} loaded)")
                    _notifications.value = mergedNotifications
                }
                
                // Sync notifications from server
                notificationRepository.sync()
                Clogger.d(TAG, "Notifications loaded successfully")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to load notifications", e)
                _error.value = "Failed to load notifications: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mark notification as seen/unseen
     */
    fun markAsSeen(notificationId: String, isSeen: Boolean = true) {
        viewModelScope.launch {
            try {
                if (isSeen) {
                    localNotificationService.markAsSeen(notificationId)
                } else {
                    notificationRepository.markAsSeen(notificationId, false)
                }
                Clogger.d(TAG, "Notification $notificationId marked as ${if (isSeen) "seen" else "unseen"}")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to mark notification as seen", e)
                _error.value = "Failed to update notification: ${e.message}"
            }
        }
    }

    /**
     * Mark notification as pinned/unpinned
     */
    fun pinNotification(notificationId: String, isPinned: Boolean = true) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsPinned(notificationId, isPinned)
                Clogger.d(TAG, "Notification $notificationId ${if (isPinned) "pinned" else "unpinned"}")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to pin notification", e)
                _error.value = "Failed to pin notification: ${e.message}"
            }
        }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                localNotificationService.deleteNotification(notificationId)
                Clogger.d(TAG, "Notification $notificationId deleted")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to delete notification", e)
                _error.value = "Failed to delete notification: ${e.message}"
            }
        }
    }

    /**
     * Mark all notifications as seen
     */
    fun markAllAsSeen() {
        viewModelScope.launch {
            try {
                // Mark all current notifications as seen
                val currentNotifications = _notifications.value
                currentNotifications.forEach { notification ->
                    if (!notification.isSeen) {
                        val updatedNotification = notification.copy(isSeen = true)
                        notificationRepository.upsert(updatedNotification)
                    }
                }
                Clogger.d(TAG, "All notifications marked as seen")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to mark all notifications as seen", e)
                _error.value = "Failed to mark all as seen: ${e.message}"
            }
        }
    }


    /**
     * Search notifications
     */
    fun searchNotifications(query: String): StateFlow<List<Notification>> {
        return notifications.map { allNotifications ->
            if (query.isBlank()) {
                allNotifications
            } else {
                allNotifications.filter { notification ->
                    notification.title.contains(query, ignoreCase = true) || 
                    notification.message.contains(query, ignoreCase = true) 
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    /**
     * Get notifications by type
     */
    fun getNotificationsByType(type: NotificationType): StateFlow<List<Notification>> {
        return notifications.map { allNotifications ->
            allNotifications.filter { notification -> notification.type == type }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    /**
     * Send notification to team
     */
    fun sendNotificationToTeam(teamId: String, title: String, message: String, type: NotificationType) {
        viewModelScope.launch {
            try {
                val authId = firebaseAuth.currentUser?.uid ?: return@launch
                val localUser = userRepository.getLocalByAuthId(authId)
                localUser?.let { user ->
                    localNotificationService.createNotification(
                        title = title,
                        message = message,
                        type = type,
                        userId = user.id,
                        teamId = teamId
                    )
                    Clogger.d(TAG, "Notification sent to team $teamId")
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to send notification to team", e)
                _error.value = "Failed to send notification: ${e.message}"
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Observe unread count
     */
    private fun observeUnreadCount() {
        viewModelScope.launch {
            try {
                unreadNotifications.collect { unreadList ->
                    _unreadCount.value = unreadList.size
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Clogger.d(TAG, "Unread count observation cancelled (normal lifecycle)")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to get unread count", e)
            }
        }
    }
    
    private fun observeNotificationEvents() {
        // Use a persistent scope that survives ViewModel lifecycle changes
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                Clogger.d(TAG, "Starting to observe notification events")
                com.ggetters.app.core.services.NotificationEventBus.newNotificationEvents.collect { notification ->
                    Clogger.d(TAG, "🎉 RECEIVED NEW NOTIFICATION EVENT: ${notification.title}")
                    Clogger.d(TAG, "Notification details: id=${notification.id}, userId=${notification.userId}, teamId=${notification.teamId}")
                    
                    // Add the new notification to the current list, avoiding duplicates
                    val currentNotifications = _notifications.value.toMutableList()
                    val existingIndex = currentNotifications.indexOfFirst { it.id == notification.id }
                    
                    if (existingIndex >= 0) {
                        // Update existing notification
                        currentNotifications[existingIndex] = notification
                        Clogger.d(TAG, "Updated existing notification: ${notification.title}")
                    } else {
                        // Add new notification to beginning
                        currentNotifications.add(0, notification)
                        Clogger.d(TAG, "Added new notification: ${notification.title}")
                    }
                    
                    _notifications.value = currentNotifications
                    Clogger.d(TAG, "Updated notifications list with EventBus notification, total: ${currentNotifications.size}")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Clogger.d(TAG, "Notification events observation cancelled")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to observe notification events", e)
            }
        }
    }

    /**
     * Refresh notifications
     */
    fun refresh() {
        loadNotifications()
    }
    
    /**
     * Get current team ID for the logged-in user
     */
    suspend fun getCurrentTeamId(): String? {
        return try {
            teamRepository.getActiveTeam().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null).value?.id
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to get current team ID", e)
            null
        }
    }

    /**
     * Observe real-time notification updates
     */
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            try {
                // Observe local notification service updates
                localNotificationService.notifications.collect { newNotification ->
                    val currentNotifications = _notifications.value.toMutableList()
                    val existingIndex = currentNotifications.indexOfFirst { it.id == newNotification.id }
                    
                    if (existingIndex >= 0) {
                        // Update existing notification
                        currentNotifications[existingIndex] = newNotification
                    } else {
                        // Add new notification
                        currentNotifications.add(0, newNotification) // Add to top
                    }
                    
                    _notifications.value = currentNotifications.sortedByDescending { it.createdAt }
                    Clogger.d(TAG, "Real-time notification update: ${newNotification.title}")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Clogger.d(TAG, "Real-time updates cancelled (normal lifecycle)")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to observe real-time updates", e)
            }
        }
    }
}
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import com.ggetters.app.data.model.NotificationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {
    
    // TODO: Backend - Add repository injection for notifications
    // private val notificationsRepository: NotificationsRepository
    
    // TODO: Backend - Add LiveData for notifications
    // private val _notifications = MutableLiveData<List<NotificationItem>>()
    // val notifications: LiveData<List<NotificationItem>> = _notifications
    
    // TODO: Backend - Add methods for notification management
    // fun loadNotifications()
    // fun markAsSeen(notificationId: Int)
    // fun deleteNotification(notificationId: Int)
    // fun pinNotification(notificationId: Int)
    // fun handleRSVPResponse(notificationId: Int, status: RSVPStatus)
}
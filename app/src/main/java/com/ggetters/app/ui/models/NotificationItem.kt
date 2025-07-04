package com.ggetters.app.ui.models

data class NotificationItem(
    val id: Int,
    val message: String,
    var isSeen: Boolean,
    val type: String
) 
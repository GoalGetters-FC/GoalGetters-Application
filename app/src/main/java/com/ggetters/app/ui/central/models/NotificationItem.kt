package com.ggetters.app.ui.central.models

data class NotificationItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val message: String,
    var isSeen: Boolean,
    val type: String, // "game", "practice", "announcement", "player", "admin"
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String = "",
    val data: Map<String, Any> = emptyMap() // Additional data for specific notification types
) 
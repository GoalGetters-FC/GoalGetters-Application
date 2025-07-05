package com.ggetters.app.ui.models

import java.util.Date

data class Event(
    val id: String,
    val title: String,
    val type: EventType,
    val date: Date,
    val time: String,
    val venue: String,
    val opponent: String? = null,
    val description: String? = null,
    val createdBy: String,
    val createdAt: Date = Date()
) {
    fun getFormattedDate(): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }
    
    fun getFormattedTime(): String {
        return time
    }
    
    fun getEventTypeDisplayName(): String {
        return type.displayName
    }
    
    fun getEventTypeColor(): String {
        return type.color
    }
}

enum class EventType(val color: String, val displayName: String, val icon: String) {
    PRACTICE("#2196F3", "Practice", "🔵"), // Blue
    GAME("#F44336", "Game", "🔴"), // Red
    OTHER("#4CAF50", "General", "🟢") // Green
} 
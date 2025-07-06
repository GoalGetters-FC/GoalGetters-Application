package com.ggetters.app.ui.central.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
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
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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
    MATCH("#F44336", "Game", "🔴"), // Red
    OTHER("#4CAF50", "General", "🟢") // Green
} 
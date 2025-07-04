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
)

enum class EventType(val color: String, val displayName: String) {
    PRACTICE("#2196F3", "Practice"), // Blue
    GAME("#F44336", "Game"), // Red
    OTHER("#4CAF50", "General") // Green
} 
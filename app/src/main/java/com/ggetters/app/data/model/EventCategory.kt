package com.ggetters.app.data.model

/**
 * Enum representing the category of an event.
 */
enum class EventCategory(val displayName: String, val icon: String, val color: String) {
    PRACTICE("Practice", "🏋️", "#2196F3"),
    MATCH("Game", "⚽", "#F44336"),
    OTHER("General", "📌", "#4CAF50"),
    TRAINING("Training", "🎯", "#FF9800")
}

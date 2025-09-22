package com.ggetters.app.data.model

import com.ggetters.app.data.model.supers.KeyedEntity
import java.util.UUID

/**
 * Represents a single cell/day in the calendar grid.
 *
 * @property id Unique identifier (UUID by default).
 * @property dayNumber Day of the month (1–31) or null for placeholders (empty cells).
 * @property events Events scheduled on this day.
 * @property isCurrentMonth Whether this day belongs to the currently displayed month.
 * @property isToday Whether this day is the current system date.
 * @property isSelected Whether this day is the currently selected date in the UI.
 *
 * @see Event
 */
data class CalendarDayItem(

    override val id: String = UUID.randomUUID().toString(),

    val dayNumber: Int? = null,
    val events: List<Event> = emptyList(),
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false

) : KeyedEntity

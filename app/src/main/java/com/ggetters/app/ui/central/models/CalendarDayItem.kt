package com.ggetters.app.ui.central.models

import com.ggetters.app.data.model.supers.KeyedEntity
import java.util.UUID

data class CalendarDayItem(

    override val id: String = UUID.randomUUID().toString(),
    
    
    // --- Attributes
    
    
    val dayNumber: Int? = null,
    val events: List<Event> = emptyList(),
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
    
    
) : KeyedEntity
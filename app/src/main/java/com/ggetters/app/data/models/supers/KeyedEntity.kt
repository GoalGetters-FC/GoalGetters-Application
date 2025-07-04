package com.ggetters.app.data.models.supers

import java.util.UUID

interface KeyedEntity {
    
    val id: String
    
    // --- Functions
    
    fun getUuid(): UUID = UUID.fromString(id)
}
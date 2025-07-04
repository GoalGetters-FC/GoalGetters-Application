package com.ggetters.app.data.models.supers

import java.time.Instant

interface AuditableEntity {
    
    val createdAt: Instant
    
    var updatedAt: Instant
    
    // --- Functions
    
    fun touch() {
        updatedAt = Instant.now()
    }
}
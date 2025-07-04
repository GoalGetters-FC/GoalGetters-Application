package com.ggetters.app.data.models.supers

import java.time.Instant

interface StashableEntity {
    
    var stashedAt: Instant?
    
    // --- Functions
    
    fun isStashed(): Boolean = (stashedAt != null)
    
    fun softDestroy() {
        if (!isStashed()) {
            stashedAt = Instant.now()
        }
    }
    
    fun softRestore() {
        if (isStashed()) {
            stashedAt = null
        }
    }
}
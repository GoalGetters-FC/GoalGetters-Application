package com.ggetters.app.data.local.entities.supers

import java.time.Instant

interface StainableEntity {

    /**
     * This should be a local-only field.
     */
    var stainedAt: Instant?
    
    // --- Functions
    
    fun isStained(): Boolean = (stainedAt != null)
    
    fun stain() {
        if (!isStained()) {
            stainedAt = Instant.now()
        }
    }
    
    fun clean() {
        if (isStained()) {
            stainedAt = null
        }
    }
}
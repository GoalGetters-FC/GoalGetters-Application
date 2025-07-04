package com.ggetters.app.data.model.supers

import java.time.Instant

/**
 * Interface definition for an entity with auditing properties.
 * Entities implementing this interface will track creation and last updated timestamps.
 *
 * Commonly used in applications where tracking data modification history is important.
 */
interface AuditableEntity {

    /**
     * The [Instant] at which the entity was created.
     * This should be set once during entity initialization and not modified afterwards.
     */
    val createdAt: Instant

    
    /**
     * The [Instant] at which the entity was last updated.
     * This value is updated every time the entity is modified.
     */
    var updatedAt: Instant
    
    
    // --- Functions

    
    /**
     * Informs the entity that it has been updated at the current [Instant].
     * Should be called before saving the entity to persist the update timestamp.
     */
    fun touch() {
        updatedAt = Instant.now()
    }
}
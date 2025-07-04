package com.ggetters.app.data.model.supers

import java.util.UUID

/**
 * Interface definition for an identifiable entity.
 * Any entity implementing this interface must provide a unique [UUID] as its identifier.
 *
 * Useful in database operations, where a unique key is required for each record.
 */
interface KeyedEntity {
    
    /**
     * Unique identifier of the entity.
     * Typically generated once and used as the primary key for database persistence.
     *
     * @see [com.ggetters.app.data.local.converters.UuidConverter]
     */
    val id: UUID
}
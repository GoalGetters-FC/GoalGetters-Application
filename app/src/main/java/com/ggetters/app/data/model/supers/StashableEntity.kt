package com.ggetters.app.data.model.supers

import java.time.Instant

/**
 * Interface definition for a soft-deletable entity.
 *
 * Entities implementing this interface support soft deletion,
 * meaning they are not permanently removed but instead marked
 * as "stashed" by setting a timestamp.
 */
interface StashableEntity {

    /**
     * The [Instant] timestamp indicating when the entity was soft-deleted.
     *
     * A `null` value indicates that the entity is active (not deleted).
     * When set, it should represent the moment of soft deletion.
     */
    var stashedAt: Instant?
    
    
    // --- Functions

    
    /**
     * Checks if the entity is currently soft-deleted.
     *
     * @return `true` if the entity has been stashed (soft-deleted), otherwise `false`.
     *
     * This provides a quick check to determine if the entity should be treated as logically removed.
     */
    fun isStashed(): Boolean = (stashedAt != null)

    
    /**
     * Marks the entity as soft-deleted by setting the [stashedAt] to the current time.
     *
     * This is useful for logical deletion where you don't want to remove the entity from storage
     * but still want it excluded from normal operations.
     *
     * This method does nothing if the entity is already stashed.
     */
    fun softDestroy() {
        if (!isStashed()) {
            stashedAt = Instant.now()
        }
    }

    
    /**
     * Reverses a soft deletion by clearing the [stashedAt] timestamp.
     *
     * This effectively "restores" the entity, making it active and usable again.
     *
     * This method does nothing if the entity is not stashed.
     */
    fun softRestore() {
        if (isStashed()) {
            stashedAt = null
        }
    }
}
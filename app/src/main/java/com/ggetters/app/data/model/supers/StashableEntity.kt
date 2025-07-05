package com.ggetters.app.data.model.supers

import com.ggetters.app.core.utils.Clogger
import java.time.Instant

/**
 * Interface definition for a soft-deletable entity.
 *
 * In the event that the entity is marked as stashed, or soft-deleted, it should
 * be treated as if it were removed from the database. The purpose of stashing a
 * database entity is to ensure that is can be recovered at a later stage.
 *
 * @property stashedAt
 * 
 * @see isStashed
 * @see isVisible
 * @see destroy
 * @see restore
 */
interface StashableEntity {
    companion object {
        private const val TAG = "StashableEntity"
    }


    // --- Fields


    /**
     * The [Instant] at which the entity was soft-deleted.
     *
     * This property contains the accurate audit history of when the entity was
     * stashed to ensure a logically sortable order. The value is `null` if the
     * entity has not been stashed.
     *
     * **Implementation:**
     *
     * ```
     * override var stashedAt: Instant? = null
     * ```
     */
    var stashedAt: Instant?


    // --- Functions


    /**
     * Determines whether the entity is stashed (soft-deleted).
     * 
     * @see destroy
     * @see restore
     */
    fun isStashed(): Boolean = (stashedAt != null)


    /**
     * Determines whether the entity is visible (not-stashed).
     * 
     * @see destroy
     * @see restore
     */
    fun isVisible(): Boolean = (stashedAt == null)


    /**
     * Attempts to soft-delete the entity by marking it as stashed at the
     * current [Instant].
     *
     * **Usage:**
     *
     * The route of the exception being thrown successfully should never be
     * reached if the entity is being handled correctly. If thrown, then a logic
     * error has likely occurred. This assurance can be bypassed by handling the
     * function safely to investigate later on.
     *
     * ```
     * if (entity.isVisible()) entity.destroy() // safe approach
     * ```
     *
     * ```
     * // safe approach (preferred)
     * try {
     *     entity.destroy()
     * } catch (e: IllegalStateException) {
     *     Clogger.e( // send the non-fatal exception to crashlytics
     *         TAG, "Failed to soft-destroy entity", e
     *     )
     * }
     * ```
     *
     * @throws IllegalStateException when an attempt to soft-destroy an already
     *         stashed object is made. This ensures that the audit log contains
     *         accurate information without losing its integrity to overwrites.
     */
    fun destroy() = when (isVisible()) {
        true -> stashedAt = Instant.now()
        else -> {
            Clogger.w(
                TAG, "You cannot soft-destroy an entity that is already stashed."
            )

            throw IllegalStateException("Entity is already stashed.")
        }
    }


    /**
     * Attempts to soft-restore the entity by clearing [stashedAt].
     *
     * **Usage:**
     *
     * The route of the exception being thrown successfully should never be
     * reached if the entity is being handled correctly. If thrown, then a logic
     * error has likely occurred. This assurance can be bypassed by handling the
     * function safely to investigate later on.
     *
     * ```
     * if (entity.isStashed()) entity.restore() // safe approach
     * ```
     *
     * ```
     * // safe approach (preferred)
     * try {
     *     entity.restore()
     * } catch (e: IllegalStateException) {
     *     Clogger.e( // send the non-fatal exception to crashlytics
     *         TAG, "Failed to soft-restore entity", e
     *     )
     * }
     * ```
     *
     * @throws IllegalStateException when an attempt to soft-destroy an already
     *         stashed object is made. This ensures that the audit log contains
     *         accurate information without losing its integrity to overwrites.
     */
    fun restore() = when (isStashed()) {
        true -> stashedAt = null
        else -> {
            Clogger.w(
                TAG, "You cannot soft-restore an entity that is already visible."
            )

            throw IllegalStateException("Entity is already visible.")
        }
    }
}
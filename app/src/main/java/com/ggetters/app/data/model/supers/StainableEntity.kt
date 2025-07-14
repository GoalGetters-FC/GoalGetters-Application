package com.ggetters.app.data.model.supers

import androidx.room.Ignore
import com.ggetters.app.core.utils.Clogger
import com.google.firebase.firestore.Exclude
import java.time.Instant

/**
 * Interface definition for an entity intended to be synced to the cloud.
 *
 * @property stainedAt
 *
 * @see isStained
 * @see isCleaned
 * @see stain
 * @see clean
 */
interface StainableEntity {
    companion object {
        private const val TAG = "StainableEntity"
    }


    // --- Fields


    /**
     * The [Instant] at which the entity was stained.
     *
     * **Note:** This should be a local-only field to ensure that a feedback
     * loop does not occur. The value is `null` if no modifications have passed
     * since the entity was last synced.
     *
     * **Implementation:**
     *
     * ```
     * @Ignore
     * @Exclude
     * override var stainedAt: Instant? = null
     * ```
     *
     * @see Ignore
     * @see Exclude
     */
    var stainedAt: Instant?


    // --- Functions


    /**
     * Determines whether the entity is stained (edited).
     *
     * @see stain
     * @see clean
     */
    fun isStained(): Boolean = (stainedAt != null)


    /**
     * Determines whether the entity is cleaned (synced).
     *
     * @see stain
     * @see clean
     */
    fun isCleaned(): Boolean = (stainedAt == null)


    /**
     * Attempts to stain the entity by marking it as stained at the current
     * [Instant].
     *
     * **Usage:**
     *
     * The route of the exception being thrown successfully should never be
     * reached if the entity is being handled correctly. If thrown, then a logic
     * error has likely occurred. This assurance can be bypassed by handling the
     * function safely to investigate later on.
     *
     * ```
     * if (entity.isCleaned()) entity.stain() // safe approach
     * ```
     *
     * ```
     * // safe approach (preferred)
     * try {
     *     entity.stain()
     * } catch (e: IllegalStateException) {
     *     Clogger.e( // send the non-fatal exception to crashlytics
     *      TAG, "Failed to stain entity", e
     *     )
     * }
     * ```
     *
     * @throws IllegalStateException when an attempt to stain an already dirty
     *         object is made. This ensures that the audit log contains accurate
     *         information without losing its integrity to overwrites.
     */
    fun stain() = when (isCleaned()) {
        true -> stainedAt = Instant.now()
        else -> {
            Clogger.w(
                TAG, "You cannot stain an entity that is already dirty (edited)."
            )

            throw IllegalStateException("Entity is already dirty.")
        }
    }


    /**
     * Attempts to clean the entity by clearing [stainedAt].
     *
     * **Usage:**
     *
     * The route of the exception being thrown successfully should never be
     * reached if the entity is being handled correctly. If thrown, then a logic
     * error has likely occurred. This assurance can be bypassed by handling the
     * function safely to investigate later on.
     *
     * ```
     * if (entity.isCleaned()) entity.clean() // safe approach
     * ```
     *
     * ```
     * // safe approach (preferred)
     * try {
     *     entity.clean()
     * } catch (e: IllegalStateException) {
     *     Clogger.e( // send the non-fatal exception to crashlytics
     *      TAG, "Failed to clean entity", e
     *     )
     * }
     * ```
     *
     * @throws IllegalStateException when an attempt to clean an already clean
     *         object is made. This ensures that the audit log contains accurate
     *         information without losing its integrity to overwrites.
     */
    fun clean() = when (isStained()) {
        true -> stainedAt = null
        else -> {
            Clogger.w(
                TAG, "You cannot clean an entity that is already clean (synced)."
            )

            throw IllegalStateException("Entity is already clean.")
        }
    }
}
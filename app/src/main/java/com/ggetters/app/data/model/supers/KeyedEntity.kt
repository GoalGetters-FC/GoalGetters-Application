package com.ggetters.app.data.model.supers

import androidx.room.PrimaryKey
import com.ggetters.app.core.utils.Clogger
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Interface definition for an identifiable entity.
 *
 * @property id
 */
interface KeyedEntity {
    companion object {
        private const val TAG = "KeyedEntity"
    }


    // --- Fields


    /**
     * Unique identifier of the entity.
     *
     * **Note:** This identifier should always be automatically generated from a
     * [UUID]. It has been constructed as a [String] with the purpose of keeping
     * the entity DBaaS independent, but should be treated as a [UUID] in every
     * operation to maintain schema integrity. This property should have a
     * unique constraint.
     *
     * It is advisable to run [isIdValid] before committing to the database.
     *
     * **Implementation:**
     *
     * See additional resources for information regarding annotation usage.
     *
     * ```
     * @PrimaryKey
     * @DocumentId
     * override val id: String = UUID.randomUUID().toString()
     * ```
     *
     * @see [PrimaryKey]
     * @see [DocumentId]
     * @see [UUID]
     */
    val id: String


    // --- Functions


    /**
     * Checks that the [id] is a valid [UUID].
     *
     * **Note:** If the ID is found to be malformed, then the entity should not
     * be committed to the database until it has been fixed to ensure the schema
     * remains uncorrupted. If this check fails, a non-fatal exception will send
     * to crashlytics, whether or not the entity is saved.
     */
    fun isIdValid(): Boolean {
        try {
            val uuid = UUID.fromString(id)
            return uuid.version() == 4
        } catch (e: IllegalArgumentException) {
            Clogger.e(
                TAG, "Invalid UUID-ID (Volatile): $id", e
            )

            return false
        }
    }
}
package com.ggetters.app.data.model.supers

import com.ggetters.app.core.utils.Clogger

/**
 * Interface definition for an entity with a shorthand unique identifier.
 * 
 * **Note:** This interface contract should only be used in conjunction with
 * the [KeyedEntity] implementation. Some functions require this metadata to
 * function. This can be mitigated with careful usage.
 *
 * @property code
 * 
 * @see generateCode
 */
interface CodedEntity {
    companion object {
        private const val TAG = "CodedEntity"
    }


    // --- Fields


    /**
     * Contains a shorthand version of the entity's unique ID.
     * 
     * This is used to create an easily shareable, but still unique, identifier
     * that does not expose the full ID of the entity. This is used to link auth
     * accounts to team profiles. 
     * 
     * **Note:** This property should have a unique constraint.
     * 
     * **Implementation:**
     * 
     * ```
     * override var code: String? = null
     * ```
     * 
     * @see generateCode
     */
    var code: String?


    // --- Functions


    /**
     * Attempts to generate the [code] for the entity based on a unique ID.
     * 
     * *See throws for important usage notice.*
     * 
     * **Usage:**
     * 
     * ```
     * // .... create entity
     * entity.generateCode()
     * ```
     * 
     * @throws IllegalStateException when the class is not a valid [KeyedEntity]
     *         database type. This interface should only be used in conjunction 
     *         with keyed types, as it depends on the relevant metadata therein.
     */
    fun generateCode() = if (this is KeyedEntity) {
        code = generateAlphanumericCode(6)
    } else {
        Clogger.e(
            TAG, "Unique code can only be generated for valid database entities."
        )
        
        throw IllegalStateException()
    }
    
    /**
     * Generates a random 6-digit alphanumeric code.
     * Uses uppercase letters and numbers (0-9, A-Z).
     */
    private fun generateAlphanumericCode(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
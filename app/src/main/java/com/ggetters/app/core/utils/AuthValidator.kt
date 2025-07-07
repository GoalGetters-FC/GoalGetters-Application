package com.ggetters.app.core.utils

import android.util.Patterns

/**
 * Helper to validate credentials against Firebase Authentication rulesets.
 */
object AuthValidator {
    
    /**
     * Validates an email address using [Patterns.EMAIL_ADDRESS].
     *
     * @return [Boolean] indicating its validity.
     */
    fun isValidEAddress(input: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }
}
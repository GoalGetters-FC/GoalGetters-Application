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

    
    /**
     * Validates a password against the Firebase Authentication ruleset.
     *
     * **Note:** This function mirrors the ruleset in Firebase Authentication.
     * It is important that these rulesets remain in sync, and that regressions
     * are accounted for in legacy accounts when these rules are changed.
     *
     * @return [Boolean] indicating its validity.
     */
    fun isValidPassword(
        input: String
    ): Boolean {
        val limitForMinLength = 6
        val limitForMaxLength = 4096

        // Compare values against validation rules

        val inputWithinLength = input.length in limitForMinLength..limitForMaxLength
        val inputHasUpperCase = input.any { it.isUpperCase() }
        val inputHasLowerCase = input.any { it.isLowerCase() }
        val inputHasNumerical = input.any { it.isDigit() }
        val inputHasAnySymbol = input.any { !it.isLetterOrDigit() }
        val inputHasNoSpacing = input.any { !it.isWhitespace() }

        return !arrayOf(
            inputWithinLength,
            inputHasUpperCase,
            inputHasLowerCase,
            inputHasNumerical,
            inputHasAnySymbol,
            inputHasNoSpacing
        ).any {
            !it
        }
    }
}
package com.ggetters.app.core.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Helper to validate credentials against Firebase Authentication rulesets.
 */
object CredentialValidator {
    private const val EMAIL_ADDRESS_REGEX = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""

    /**
     * Validates an email address. Uses Android Patterns when available; falls back to regex in JVM tests.
     */
    fun isValidEAddress(input: String): Boolean {
        val fallbackValid = Pattern.compile(EMAIL_ADDRESS_REGEX).matcher(input).matches()
        val androidValid = try {
            val pattern = Patterns.EMAIL_ADDRESS
            pattern != null && pattern.matcher(input).matches()
        } catch (_: Throwable) {
            false
        }
        return androidValid || fallbackValid
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
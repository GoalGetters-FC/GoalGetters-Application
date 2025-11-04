package com.ggetters.app.core.validation.statute.string

import android.util.Patterns
import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw
import java.util.regex.Pattern

typealias AsEmailAddress = AsEmailAddressStringValidationLaw

class AsEmailAddressStringValidationLaw : ValidationLaw<String> {
    companion object {
        // Relaxed regex suitable for common email formats and JVM unit tests
        private const val EMAIL_ADDRESS_REGEX = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    }

    override fun checkFor(value: String): ValidationError? {
        val fallbackValid = Pattern.compile(EMAIL_ADDRESS_REGEX).matcher(value).matches()
        val androidValid = try {
            val pattern = Patterns.EMAIL_ADDRESS
            pattern != null && pattern.matcher(value).matches()
        } catch (_: Throwable) {
            false
        }
        val isValid = androidValid || fallbackValid

        return if (!isValid) ValidationError.String.INVALID_EMAIL_ADDRESS else null
    }
}
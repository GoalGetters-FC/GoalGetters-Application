package com.ggetters.app.core.validation.statute.string

import android.util.Patterns
import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw
import java.util.regex.Pattern

typealias AsEmailAddress = AsEmailAddressStringValidationLaw

class AsEmailAddressStringValidationLaw : ValidationLaw<String> {
    companion object {
        // Simple, permissive regex suitable for JVM unit tests when Android Patterns is unavailable
        private const val EMAIL_ADDRESS_REGEX = "[a-zA-Z0-9+._%\\-+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    }

    override fun checkFor(value: String): ValidationError? {
        val isValid = try {
            // Patterns.EMAIL_ADDRESS can be null or uninitialized on the JVM
            val pattern = Patterns.EMAIL_ADDRESS
            pattern != null && pattern.matcher(value).matches()
        } catch (_: Throwable) {
            // Fallback for JVM tests
            Pattern.compile(EMAIL_ADDRESS_REGEX).matcher(value).matches()
        }

        return if (!isValid) ValidationError.String.INVALID_EMAIL_ADDRESS else null
    }
}
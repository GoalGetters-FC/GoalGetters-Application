package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

/**
 * Validation law to check if password confirmation matches the original password.
 * This should be used for password confirmation fields.
 */
class PasswordMatchStringValidationLaw(
    private val originalPassword: String
) : ValidationLaw<String> {
    
    override fun checkFor(value: String): ValidationError? {
        return when {
            value.trim() != originalPassword.trim() -> ValidationError.String.PASSWORDS_DONT_MATCH
            else -> null
        }
    }
}

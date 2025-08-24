package com.ggetters.app.core.validation.statute.string

import android.util.Patterns
import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias AsEmailAddress = AsEmailAddressStringValidationLaw

class AsEmailAddressStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            true -> ValidationError.String.INVALID_EMAIL_ADDRESS
            else -> null
        }
    }
}
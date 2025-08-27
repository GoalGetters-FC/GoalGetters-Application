package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias ExcludeNumbers = ExcludeNumbersStringValidationLaw

class ExcludeNumbersStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isDigit() }) {
            true -> ValidationError.String.EXCLUDE_NUMBERS
            else -> null
        }
    }
}
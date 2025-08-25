package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias ExcludeUppercase = ExcludeUppercaseStringValidationLaw

class ExcludeUppercaseStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isUpperCase() }) {
            true -> ValidationError.String.EXCLUDE_UPPERCASE
            else -> null
        }
    }
}
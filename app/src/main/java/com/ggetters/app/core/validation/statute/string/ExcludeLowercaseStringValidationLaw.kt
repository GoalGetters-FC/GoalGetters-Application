package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias ExcludeLowercase = ExcludeLowercaseStringValidationLaw

class ExcludeLowercaseStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isLowerCase() }) {
            true -> ValidationError.String.EXCLUDE_LOWERCASE
            else -> null
        }
    }
}
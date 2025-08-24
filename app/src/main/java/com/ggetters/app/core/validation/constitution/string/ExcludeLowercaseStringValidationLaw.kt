package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias ExcludeLowercase = ExcludeLowercaseStringValidationLaw

class ExcludeLowercaseStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isLowerCase() }) {
            true -> ValidationError.String.EXCLUDE_LOWERCASE
            else -> null
        }
    }
}
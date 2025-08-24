package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias ExcludeWhitespace = ExcludeWhitespaceStringValidationLaw

class ExcludeWhitespaceStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isWhitespace() }) {
            true -> ValidationError.String.EXCLUDE_WHITESPACE
            else -> null
        }
    }
}
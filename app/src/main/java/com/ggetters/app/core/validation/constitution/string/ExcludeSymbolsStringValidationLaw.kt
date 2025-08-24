package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias ExcludeSymbols = ExcludeSymbolsStringValidationLaw

class ExcludeSymbolsStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { !(it.isLetterOrDigit()) }) {
            true -> ValidationError.String.EXCLUDE_SYMBOLS
            else -> null
        }
    }
}
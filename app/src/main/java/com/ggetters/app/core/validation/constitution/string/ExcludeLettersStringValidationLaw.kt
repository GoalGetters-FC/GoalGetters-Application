package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias ExcludeLetters = ExcludeLettersStringValidationLaw

class ExcludeLettersStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it.isLetter() }) {
            true -> ValidationError.String.EXCLUDE_LETTERS
            else -> null
        }
    }
}
package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias ExcludeCharacters = ExcludeCharactersStringValidationLaw

class ExcludeCharactersStringValidationLaw(
    private vararg val characters: Char
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return when (value.any { it in characters }) {
            true -> ValidationError.String.EXCLUDE_CUSTOM_CHARACTERS
            else -> null
        }
    }
}
package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias IncludeCharacters = IncludeCharactersStringValidationLaw

class IncludeCharactersStringValidationLaw(
    private vararg val characters: Char
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        characters.forEach { char ->
            if (!value.contains(char)) {
                return ValidationError.String.INCLUDE_CUSTOM_CHARACTERS
            }
        }

        return null
    }
}
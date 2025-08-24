package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias IncludeLetters = IncludeLettersStringValidationLaw

class IncludeLettersStringValidationLaw(
    private val min: Int = 1,
    private val max: Int? = null,
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        if (max != null) {
            check(max >= min)
        }

        val limit = max ?: Int.MAX_VALUE
        val count = value.count { it.isLetter() }
        return when (count) {
            in (min..limit) -> null
            else -> {
                ValidationError.String.INCLUDE_LETTERS
            }
        }
    }
}
package com.ggetters.app.core.validation.constitution.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.constitution.ValidationLaw

typealias MaxLength = MaxLengthStringValidationLaw

class MaxLengthStringValidationLaw(
    private val length: Int
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        check(length >= 0)
        return when (value.length > length) {
            true -> ValidationError.String.REQUIRE_MAX_CHARS
            else -> null
        } 
    }
}
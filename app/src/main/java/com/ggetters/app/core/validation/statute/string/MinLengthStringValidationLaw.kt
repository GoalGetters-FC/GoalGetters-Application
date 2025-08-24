package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias MinLength = MinLengthStringValidationLaw

class MinLengthStringValidationLaw(
    private val length: Int
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        check(length >= 0)
        return when (value.length < length) {
            true -> ValidationError.String.REQUIRE_MIN_CHARS
            else -> null
        }
    }
}
package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias Length = LengthStringValidationLaw

class LengthStringValidationLaw(
    private val min: Int,
    private val max: Int,
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        check(max >= min)
        MinLength(min).checkFor(value)?.let { return it }
        MaxLength(max).checkFor(value)?.let { return it }
        return null
    }
}
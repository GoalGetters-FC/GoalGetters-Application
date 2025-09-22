package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias IncludeUppercase = IncludeUppercaseStringValidationLaw

class IncludeUppercaseStringValidationLaw(
    private val min: Int = 1,
    private val max: Int? = null,
) : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        if (max != null) {
            check(max >= min)
        }

        val limit = max ?: Int.MAX_VALUE
        val count = value.count { it.isUpperCase() }
        return when (count) {
            in (min..limit) -> null
            else -> {
                ValidationError.String.INCLUDE_UPPERCASE
            }
        }
    }
}
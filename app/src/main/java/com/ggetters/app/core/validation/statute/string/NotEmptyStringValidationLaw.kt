package com.ggetters.app.core.validation.statute.string

import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw

typealias NotEmpty = NotEmptyStringValidationLaw

class NotEmptyStringValidationLaw : ValidationLaw<String> {
    override fun checkFor(value: String): ValidationError? {
        return MinLength(1).checkFor(value)
    }
}
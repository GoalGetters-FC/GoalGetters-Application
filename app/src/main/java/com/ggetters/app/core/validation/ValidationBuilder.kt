package com.ggetters.app.core.validation

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.constitution.ValidationLaw

open class ValidationBuilder<T> private constructor(
    private val valueToValidate: T
) {
    companion object {
        private const val TAG = "ValidationBuilder"

        // --- Extensions

        fun forString(value: String) = ValidationBuilder(value)
        fun forNumber(value: Number) = ValidationBuilder(value)
    }


// --- Validation


    protected val laws = mutableListOf<ValidationLaw<T>>()


    fun register(law: ValidationLaw<T>) = apply {
        laws.add(law)
    }


    fun validate(): Final<Unit, ValidationError> {
        for (rule in laws) {
            val violation = rule.checkFor(valueToValidate)
            if (violation != null) {
                return Final.Failure(violation)
            }
        }

        return Final.Success(Unit)
    }
}
package com.ggetters.app.core.validation.constitution

import com.ggetters.app.core.validation.ValidationError

fun interface ValidationLaw<T> {
    fun checkFor(value: T): ValidationError?
}
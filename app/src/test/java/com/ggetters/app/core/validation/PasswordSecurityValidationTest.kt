package com.ggetters.app.core.validation

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.statute.string.AsFirebaseCredential
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordSecurityValidationTest {
    @Test
    fun weakPassword_failsFast() {
        val result = ValidationBuilder.forString("abc")
            .register(AsFirebaseCredential())
            .validate()

        assertTrue(result is Final.Failure)
    }

    @Test
    fun strongPassword_passes() {
        val result = ValidationBuilder.forString("Abcdef1!")
            .register(AsFirebaseCredential())
            .validate()

        assertTrue(result is Final.Success)
    }
}



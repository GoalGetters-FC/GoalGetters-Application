package com.ggetters.app.core.validation

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.statute.string.AsEmailAddress
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidationTest {
    @Test
    fun invalidEmail_fails() {
        val result = ValidationBuilder.forString("user@@domain..com")
            .register(AsEmailAddress())
            .validate()

        assertTrue(result is Final.Failure)
    }

    @Test
    fun validEmail_passes() {
        val result = ValidationBuilder.forString("user@example.com")
            .register(AsEmailAddress())
            .validate()

        assertTrue(result is Final.Success)
    }
}



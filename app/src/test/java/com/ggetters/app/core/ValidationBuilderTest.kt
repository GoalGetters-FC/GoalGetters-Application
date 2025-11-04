package com.ggetters.app.core

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.ValidationBuilder
import com.ggetters.app.core.validation.ValidationError
import com.ggetters.app.core.validation.statute.ValidationLaw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationBuilderTest {
    private class AlwaysPassLaw : ValidationLaw<String> {
        override fun checkFor(value: String) = null
    }

    private class AlwaysFailLaw(private val error: ValidationError) : ValidationLaw<String> {
        override fun checkFor(value: String) = error
    }

    @Test
    fun validate_successWhenAllPass() {
        val result = ValidationBuilder.forString("abc")
            .register(AlwaysPassLaw())
            .register(AlwaysPassLaw())
            .validate()

        assertTrue(result is Final.Success)
    }

    @Test
    fun validate_stopsAtFirstFailure() {
        val err1 = ValidationError.String.REQUIRE_MIN_CHARS
        val err2 = ValidationError.String.REQUIRE_MAX_CHARS

        val result = ValidationBuilder.forString("abc")
            .register(AlwaysFailLaw(err1))
            .register(AlwaysFailLaw(err2))
            .validate()

        assertTrue(result is Final.Failure)
        val failure = result as Final.Failure<*, ValidationError>
        assertEquals(err1, failure.problem)
    }
}

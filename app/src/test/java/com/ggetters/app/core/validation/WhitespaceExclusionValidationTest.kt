package com.ggetters.app.core.validation

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.statute.string.ExcludeWhitespace
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitespaceExclusionValidationTest {
    @Test
    fun inputWithWhitespace_fails() {
        val result = ValidationBuilder.forString("abc def")
            .register(ExcludeWhitespace())
            .validate()

        assertTrue(result is Final.Failure)
    }

    @Test
    fun inputWithoutWhitespace_passes() {
        val result = ValidationBuilder.forString("abcdef")
            .register(ExcludeWhitespace())
            .validate()

        assertTrue(result is Final.Success)
    }
}

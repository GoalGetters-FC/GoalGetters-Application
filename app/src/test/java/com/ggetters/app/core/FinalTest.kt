package com.ggetters.app.core

import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.models.results.FinalError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalTest {
    @Test
    fun onSuccess_executesCallback() {
        val result: Final<Int, FinalError> = Final.Success(42)
        var called = false

        result.onSuccess { value ->
            called = true
            assertEquals(42, value)
        }

        assertTrue(called)
    }

    @Test
    fun onFailure_executesCallback() {
        val error = object : FinalError {}
        val result: Final<Int, FinalError> = Final.Failure(error)
        var called = false

        result.onFailure { err ->
            called = true
            assertTrue(err === error)
        }

        assertTrue(called)
    }
}


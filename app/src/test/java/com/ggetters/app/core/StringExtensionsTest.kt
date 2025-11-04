package com.ggetters.app.core

import com.ggetters.app.core.extensions.kotlin.emptyString
import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun emptyString_returnsEmpty() {
        assertEquals("", emptyString())
    }
}



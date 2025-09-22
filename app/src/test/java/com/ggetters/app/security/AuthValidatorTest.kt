package com.ggetters.app.security

import com.ggetters.app.core.utils.AuthValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `password too short is rejected`() {
        assertFalse(AuthValidator.isValidPassword("Ab1!"))
    }

    @Test
    fun `password without uppercase is rejected`() {
        assertFalse(AuthValidator.isValidPassword("lowercase1!"))
    }

    @Test
    fun `password without digit is rejected`() {
        assertFalse(AuthValidator.isValidPassword("NoDigits!"))
    }

    @Test
    fun `password with whitespace is rejected`() {
        assertFalse(AuthValidator.isValidPassword("Bad Pass1!"))
    }

    @Test
    fun `strong password is accepted`() {
        assertTrue(AuthValidator.isValidPassword("Str0ng!Pass"))
    }
}

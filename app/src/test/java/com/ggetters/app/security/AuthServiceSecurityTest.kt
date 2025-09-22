package com.ggetters.app.security

import com.ggetters.app.core.services.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthServiceSecurityTest {

    @Test
    fun `isUserSignedIn reports true when currentUser is present`() {
        val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
        val user = mockk<FirebaseUser>(relaxed = true)
        every { firebaseAuth.currentUser } returns user

        val service = AuthService(firebaseAuth)
        assertTrue(service.isUserSignedIn())
    }

    @Test
    fun `isUserSignedIn reports false when currentUser is null`() {
        val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
        every { firebaseAuth.currentUser } returns null

        val service = AuthService(firebaseAuth)
        assertFalse(service.isUserSignedIn())
    }

    @Test
    fun `logout calls FirebaseAuth signOut`() {
        val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
        val service = AuthService(firebaseAuth)

        service.logout()

        verify(exactly = 1) { firebaseAuth.signOut() }
    }
}

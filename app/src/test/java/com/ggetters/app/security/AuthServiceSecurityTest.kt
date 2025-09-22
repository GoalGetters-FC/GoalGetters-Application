package com.ggetters.app.security

import com.ggetters.app.core.services.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthServiceSecurityTest {

    @Before
    fun setup() {
        System.setProperty("DISABLE_CLOGGER", "true")
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>(), any()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>(), any()) } returns 0
    }

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

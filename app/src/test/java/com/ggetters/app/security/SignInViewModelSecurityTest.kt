package com.ggetters.app.security

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.startup.viewmodels.SignInViewModel
import io.mockk.*
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SignInViewModelSecurityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `invalid email or password yields Failure without hitting AuthService`() {
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        val authService = mockk<AuthService>(relaxed = true)
        val vm = SignInViewModel(authService)
        val observer = mockk<Observer<UiState>>(relaxed = true)
        vm.uiState.observeForever(observer)

        // Invalid email
        vm.signIn("invalid-email", "Str0ng!Pass")
        // Invalid password
        vm.signIn("user@example.com", "weak")

        verify { observer.onChanged(ofType(UiState.Failure::class)) }
        coVerify(exactly = 0) { authService.signInAsync(any(), any()) }

        unmockkAll()
    }
}

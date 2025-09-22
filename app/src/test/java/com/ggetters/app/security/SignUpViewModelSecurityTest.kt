package com.ggetters.app.security

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.startup.viewmodels.SignUpViewModel
import io.mockk.*
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SignUpViewModelSecurityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `invalid or mismatched passwords yields Failure without hitting AuthService`() {
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        val authService = mockk<AuthService>(relaxed = true)
        val vm = SignUpViewModel(authService)
        val observer = mockk<Observer<UiState>>(relaxed = true)
        vm.uiState.observeForever(observer)

        // Invalid password (too weak)
        vm.signUp("user@example.com", "weak", "weak")
        // Mismatched passwords
        vm.signUp("user@example.com", "Str0ng!Pass", "Str0ng!Pas$Diff")

        verify(atLeast = 1) { observer.onChanged(ofType(UiState.Failure::class)) }
        coVerify(exactly = 0) { authService.signUpAsync(any(), any()) }

        unmockkAll()
    }
}

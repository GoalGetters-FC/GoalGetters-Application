package com.ggetters.app.security

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.startup.viewmodels.SignUpViewModel
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SignUpViewModelSecurityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `invalid or mismatched passwords emit Failure and do not call AuthService`() {
        mockkStatic("android.util.Log")
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any<String>()) } returns 0

        val authService = mockk<AuthService>(relaxed = true)
        val vm = SignUpViewModel(authService)
        val observer = mockk<Observer<UiState>>(relaxed = true)
        vm.uiState.observeForever(observer)

        vm.signUp("user@example.com", "weak", "weak")
        vm.signUp("user@example.com", "Str0ng!Pass", "Str0ng!Pas\$Diff")

        verify(atLeast = 1) { observer.onChanged(io.mockk.ofType(UiState.Failure::class)) }
        coVerify(exactly = 0) { authService.signUpAsync(any(), any()) }
    }
}

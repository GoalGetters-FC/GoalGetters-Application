package com.ggetters.app.security

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.startup.viewmodels.SignInViewModel
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
class SignInViewModelSecurityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `invalid input yields Failure and does not call AuthService`() {
        mockkStatic("android.util.Log")
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any<String>()) } returns 0

        val authService = mockk<AuthService>(relaxed = true)
        val vm = SignInViewModel(authService)
        val observer = mockk<Observer<UiState>>(relaxed = true)
        vm.uiState.observeForever(observer)

        vm.signIn("bad-email", "weak")

        verify { observer.onChanged(io.mockk.ofType(UiState.Failure::class)) }
        coVerify(exactly = 0) { authService.signInAsync(any(), any()) }
    }
}

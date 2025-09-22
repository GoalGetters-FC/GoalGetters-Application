package com.ggetters.app.security

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.startup.viewmodels.ForgotPasswordViewModel
import io.mockk.*
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ForgotPasswordViewModelSecurityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `invalid email yields Failure without calling AuthService`() {
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        val authService = mockk<AuthService>(relaxed = true)
        val vm = ForgotPasswordViewModel(authService)
        val observer = mockk<Observer<UiState>>(relaxed = true)
        vm.uiState.observeForever(observer)

        vm.sendEmail("invalid-email")

        verify { observer.onChanged(ofType(UiState.Failure::class)) }
        coVerify(exactly = 0) { authService.sendCredentialChangeEmailAsync(any()) }

        unmockkAll()
    }
}

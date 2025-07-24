package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.core.utils.AuthValidator
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }


// --- Fields


    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState


// --- Contracts


    fun sendEmail(
        emailAddress: String
    ) = viewModelScope.launch {
        try { // Validate input
            require(AuthValidator.isValidEAddress(emailAddress))
        } catch (e: IllegalArgumentException) {
            Clogger.d(
                TAG, "Caught validation errors"
            )

            _uiState.value = Failure(e.message.toString())
            return@launch
        }

        _uiState.value = Loading
        Clogger.i(
            TAG, "Sending password reset instruction email to: $emailAddress"
        )

        // Communicate

        runCatching {
            val milliseconds = 3_000L
            withTimeout(milliseconds) {
                authService.sendCredentialChangeEmailAsync(emailAddress)
            }
        }.apply {
            onSuccess { user ->
                Clogger.d(
                    TAG, "Attempt to send the email was a success!"
                )

                _uiState.value = Success
            }

            onFailure { exception ->
                Clogger.d(
                    TAG, "Attempt to send the email was a failure!"
                )

                _uiState.value = Failure(
                    exception.message.toString()
                )
            }
        }
    }
}
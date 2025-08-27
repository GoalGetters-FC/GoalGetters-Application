package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
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
class SignUpViewModel @Inject constructor(
    private val authService: AuthenticationService
) : ViewModel() {
    companion object {
        private const val TAG = "SignUpViewModel"
    }


    // --- Fields


    val form = SignUpFormViewModel()


    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState


    // --- Contract


    fun signUp() {
        viewModelScope.launch {
            form.validateForm()
            if (!(form.isFormValid.value)) {
                return@launch
            }

            try { // Validate input
                require( // Confirm that passwords match
                    (form.formState.value.passwordDefault.value.trim() == form.formState.value.passwordConfirm.value.trim())
                )
            } catch (e: IllegalArgumentException) {
                Clogger.d(
                    TAG, "Caught validation errors"
                )

                _uiState.value = Failure(e.message.toString())
                return@launch
            }

            _uiState.value = Loading
            Clogger.i(
                TAG, "Signing-up user with email: ${form.formState.value.identity.value}"
            )

            // Authenticate

            runCatching {
                val milliseconds = 3_000L
                withTimeout(milliseconds) {
                    authService.signUpAsync(
                        email = form.formState.value.identity.value.trim(),
                        password = form.formState.value.passwordDefault.value.trim()
                    )
                }
            }.apply {
                onSuccess {
                    Clogger.d(
                        TAG, "Attempt to authenticate was a success!"
                    )

                    _uiState.value = Success
                }

                onFailure { exception ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a failure!"
                    )

                    _uiState.value = Failure(
                        exception.message.toString()
                    )
                }
            }
        }
    }
}
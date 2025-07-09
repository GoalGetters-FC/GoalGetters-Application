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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SignUpViewModel(
    private val authService: AuthService = AuthService(FirebaseAuth.getInstance())
) : ViewModel() {
    companion object {
        private const val TAG = "SignUpViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState


    // --- Contract


    fun signUp(
        email: String, defaultPassword: String, confirmPassword: String
    ) = viewModelScope.launch {
        try { // Validate input
            require(AuthValidator.isValidEAddress(email))
            require(AuthValidator.isValidPassword(defaultPassword))
            require(AuthValidator.isValidPassword(confirmPassword))
            require( // Confirm that passwords match
                (defaultPassword == confirmPassword)
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
            TAG, "Signing-up user with email: $email"
        )

        // Authenticate

        runCatching {
            val milliseconds = 3_000L
            withTimeout(milliseconds) {
                authService.signUpAsync(email, defaultPassword)
            }
        }.apply {
            onSuccess { user ->
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
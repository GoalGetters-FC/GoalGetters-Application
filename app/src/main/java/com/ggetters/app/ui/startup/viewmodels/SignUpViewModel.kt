package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.core.utils.AuthValidator
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.startup.models.SignUpUiState
import com.ggetters.app.ui.startup.models.SignUpUiState.Loading
import com.ggetters.app.ui.startup.models.SignUpUiState.Success
import com.ggetters.app.ui.startup.models.SignUpUiState.Failure
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SignUpViewModel(
    private val authService: AuthService
) : ViewModel() {
    companion object {
        private const val TAG = "SignUpViewModel"
    }


    // --- Fields


    private val _uiState = MutableLiveData<SignUpUiState>()
    val uiState: LiveData<SignUpUiState> = _uiState


    // --- Contract


    fun signUp(
        email: String, defaultPassword: String, confirmPassword: String
    ) = viewModelScope.launch {
        try {
            require(AuthValidator.isValidEAddress(email))
            require(AuthValidator.isValidPassword(defaultPassword))
            require(AuthValidator.isValidPassword(confirmPassword))
            require(
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
            withTimeout(3_000) {
                // TODO: ...
            }
        }.apply {
            onSuccess { user ->
                Clogger.d(
                    TAG, "Attempt to authenticate was a success!"
                )
                
                // TODO: ...
                
                _uiState.value = Success
            }
            
            onFailure { failure ->
                Clogger.d(
                    TAG, "Attempt to authenticate was a failure!"
                )
                
                // TODO: ...
                
                _uiState.value = Failure(failure.message.toString())
            }
        }
    }
}
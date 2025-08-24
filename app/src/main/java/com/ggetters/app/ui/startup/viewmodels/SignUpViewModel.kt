package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.CredentialValidator
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import com.ggetters.app.ui.shared.models.ValidatableForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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


    val signUpForm = SignUpFormViewModel()
    private val formList: List<ValidatableForm> = listOf(
        signUpForm
    )

    
    val isAllFormsValid = combine(
        formList.map { it.isFormValid }
    ) {
        it.all { isValid -> 
            isValid 
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )


    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState


    // --- Contract


    fun signUp(
        email: String, defaultPassword: String, confirmPassword: String
    ) = viewModelScope.launch {
        if (!(signUpForm.isFormValid.value)) {
            _uiState.value = Failure("Form Invalid")
            return@launch
        }

        try { // Validate input
            require(CredentialValidator.isValidEAddress(email))
            require(CredentialValidator.isValidPassword(defaultPassword))
            require(CredentialValidator.isValidPassword(confirmPassword))
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
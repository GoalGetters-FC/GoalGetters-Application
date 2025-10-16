package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val userRepository: UserRepository
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
                onSuccess { firebaseUser ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a success!"
                    )

                    // Create user entity in local database
                    try {
                        val user = User(
                            id = firebaseUser.uid,
                            authId = firebaseUser.uid,
                            teamId = null, // Will be set during onboarding
                            name = "",
                            surname = "",
                            email = firebaseUser.email ?: form.formState.value.identity.value.trim(),
                            role = UserRole.FULL_TIME_PLAYER,
                            status = UserStatus.ACTIVE,
                            joinedAt = Instant.now()
                        )
                        
                        userRepository.upsert(user)
                        Clogger.d(TAG, "User entity created successfully")
                        
                        _uiState.value = Success
                    } catch (e: Exception) {
                        Clogger.e(TAG, "Failed to create user entity: ${e.message}", e)
                        _uiState.value = Failure("Account created but profile setup failed. Please try again.")
                    }
                }

                onFailure { exception ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a failure!"
                    )

                    val errorMessage = when {
                        exception.message?.contains("already in use", ignoreCase = true) == true -> 
                            "An account with this email already exists. Please sign in instead."
                        exception.message?.contains("weak password", ignoreCase = true) == true -> 
                            "Password is too weak. Please use a stronger password with at least 6 characters, including uppercase, lowercase, numbers, and symbols."
                        exception.message?.contains("invalid email", ignoreCase = true) == true -> 
                            "Invalid email address. Please enter a valid email address."
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your internet connection and try again."
                        exception.message?.contains("timeout", ignoreCase = true) == true -> 
                            "Request timed out. Please try again."
                        else -> 
                            "Sign-up failed. Please try again."
                    }

                    _uiState.value = Failure(errorMessage)
                }
            }
        }
    }
}
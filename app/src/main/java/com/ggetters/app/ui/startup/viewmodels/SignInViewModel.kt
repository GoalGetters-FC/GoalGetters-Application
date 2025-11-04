package com.ggetters.app.ui.startup.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.core.services.GoogleAuthenticationClient
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.ui.shared.models.UiState
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SignInViewModel"
    }


// --- Fields


    @Inject
    lateinit var ssoClient: GoogleAuthenticationClient


    val form = SignInFormViewModel()


    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    
    // Navigation state to determine where to go after successful sign-in
    private val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState
    
    sealed class NavigationState {
        object ToOnboarding : NavigationState()
        object ToHome : NavigationState()
    }


// --- Contract


    fun signIn() {
        viewModelScope.launch {
            form.validateForm()
            if (!(form.isFormValid.value)) {
                return@launch
            }

            _uiState.value = Loading
            Clogger.i(
                TAG, "Signing-in user with email: ${form.formState.value.identity.value}"
            )

            // Authenticate

            runCatching {
                val milliseconds = 3_000L
                withTimeout(milliseconds) {
                    authService.signInAsync(
                        email = form.formState.value.identity.value.trim(),
                        password = form.formState.value.password.value.trim()
                    )
                }
            }.apply {
                onSuccess { user ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a success!"
                    )

                    // Check if user has a team assigned
                    checkUserTeamStatus(user.uid)
                }

                onFailure { exception ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a failure!"
                    )

                    val errorMessage = when {
                        exception.message?.contains("incorrect", ignoreCase = true) == true -> 
                            "Incorrect email or password. Please check your credentials and try again."
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your internet connection and try again."
                        exception.message?.contains("timeout", ignoreCase = true) == true -> 
                            "Request timed out. Please try again."
                        exception.message?.contains("user not found", ignoreCase = true) == true -> 
                            "No account found with this email address. Please sign up first."
                        exception.message?.contains("too many requests", ignoreCase = true) == true -> 
                            "Too many failed attempts. Please try again later."
                        else -> 
                            "Sign-in failed. Please check your credentials and try again."
                    }

                    _uiState.value = Failure(errorMessage)
                }
            }
        }
    }
    
    /**
     * Check if the user has a team assigned and determine navigation destination
     */
    private fun checkUserTeamStatus(userId: String) {
        viewModelScope.launch {
            try {
                // Ensure latest team data before making decisions
                runCatching { teamRepository.sync() }

                // 1) Prefer an already active team if present locally
                val activeTeam = teamRepository.getActiveTeam().first()
                if (activeTeam != null) {
                    Clogger.d(TAG, "Active team already set: ${'$'}{activeTeam.name}")
                    _navigationState.value = NavigationState.ToHome
                    _uiState.value = Success
                    return@launch
                }

                // 2) Fall back to user's stored teamId
                val user = userRepository.getById(userId)
                if (user != null && !user.teamId.isNullOrBlank()) {
                    val team = teamRepository.getById(user.teamId)
                    if (team != null) {
                        teamRepository.setActiveTeam(team)
                        Clogger.d(TAG, "Set active team from user.teamId: ${'$'}{team.name}")
                        _navigationState.value = NavigationState.ToHome
                        _uiState.value = Success
                        return@launch
                    }
                }

                // 3) Otherwise choose a team from the user's teams list
                val userTeams = teamRepository.getTeamsForCurrentUser().first()
                if (userTeams.isNotEmpty()) {
                    val firstTeam = userTeams.first()
                    teamRepository.setActiveTeam(firstTeam)
                    Clogger.d(TAG, "Set first available team as active: ${'$'}{firstTeam.name}")

                    // Update user's teamId if missing
                    if (user != null && user.teamId.isNullOrBlank()) {
                        val updatedUser = user.copy(teamId = firstTeam.id)
                        userRepository.upsert(updatedUser)
                    }

                    _navigationState.value = NavigationState.ToHome
                    _uiState.value = Success
                } else {
                    // 4) No teams → onboarding
                    Clogger.d(TAG, "User has no teams - navigating to onboarding")
                    _navigationState.value = NavigationState.ToOnboarding
                    _uiState.value = Success
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Error checking user team status: ${e.message}", e)
                // Default to onboarding if there's an error
                _navigationState.value = NavigationState.ToOnboarding
                _uiState.value = Success
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun googleSignIn() = viewModelScope.launch {
        _uiState.value = Loading
        Clogger.i(
            TAG, "Signing-in user with Google SSO"
        )

        runCatching {
            val milliseconds = 30_000L
            withTimeout(milliseconds) {
                ssoClient.executeAuthenticationTransactionAsync()
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
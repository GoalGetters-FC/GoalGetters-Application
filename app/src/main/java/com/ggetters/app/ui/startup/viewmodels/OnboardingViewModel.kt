package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.CodeGenerationUtils
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives onboarding actions: creating or joining a team, then navigating to Home.
 *
 * Notes:
 * - Matches TeamRepository interface you provided (createTeam(Team), joinTeam(teamId), getByCode(code), joinOrCreateTeam(code)).
 * - UI state via StateFlow; one-off events via Channel.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/viewmodel">Android ViewModel</a>
 * @see <a href="https://developer.android.com/kotlin/flow/stateflow-and-sharedflow">StateFlow & SharedFlow</a>
 * @see <a href="https://developer.android.com/training/dependency-injection/hilt-android">Hilt</a>
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val codeGenerationUtils: CodeGenerationUtils
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
    }

    data class UiState(
        val isBusy: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface UiEvent {
        data object NavigateHome : UiEvent
        data class Toast(val message: String) : UiEvent
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Create a team from a name and make it active.
     *
     * Adjust the Team(...) construction to your data class’ required params/defaults.
     * After creation, we set it active and call sync() to push changes if online.
     *
     * @see <a href="https://developer.android.com/topic/libraries/architecture/repositories">Repositories</a>
     */
    fun createTeam(teamName: String) = viewModelScope.launch {
        val name = teamName.trim()
        if (name.isBlank()) {
            _events.send(UiEvent.Toast("Please enter a team name."))
            return@launch
        }

        _state.update { it.copy(isBusy = true, errorMessage = null) }
        runCatching {
            // Generate collision-safe team code
            val teamCode = codeGenerationUtils.generateCollisionSafeTeamCode()
            val toCreate = Team(name = name, code = teamCode)
            val created: Team = teamRepository.createTeam(toCreate)
            teamRepository.setActiveTeam(created)
            teamRepository.sync()
            created
        }.onSuccess { created ->
            Clogger.i(TAG, "Created team ${created.name} (${created.id}) and set active")
            _state.update { it.copy(isBusy = false) }
            _events.send(UiEvent.NavigateHome)
        }.onFailure { e ->
            Clogger.e(TAG, "Failed to create team", e)
            _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Failed to create team") }
            _events.send(UiEvent.Toast("Could not create team: ${e.message ?: "Unknown error"}"))
        }
    }

    /**
     * Join a team using a join code (and optional userCode if your backend uses it).
     *
     * Flow:
     * 1) Try find team by code.
     * 2) If found → joinTeam(team.id).
     * 3) If not found → fall back to joinOrCreateTeam(code) (if your server allows creating via code).
     * 4) Set active + sync.
     *
     * The provided TeamRepository API does not accept userCode, so we ignore it here.
     * If you later extend the repo, thread userCode through accordingly.
     */
    fun joinTeam(teamCode: String, userCode: String) = viewModelScope.launch {
        val code = teamCode.trim()
        if (code.isBlank()) {
            _events.send(UiEvent.Toast("Team code is required."))
            return@launch
        }
        // Accept 6-digit alphanumeric codes (0-9, A-Z)
        if (!code.matches(Regex("^[A-Z0-9]{6}$"))) {
            _events.send(UiEvent.Toast("Team code must be 6 characters (letters and numbers only)."))
            return@launch
        }

        _state.update { it.copy(isBusy = true, errorMessage = null) }
        runCatching {
            val existing: Team? = teamRepository.getByCode(code)
            val joined: Team = if (existing != null) {
                teamRepository.joinTeam(existing.id)
                existing
            } else {
                // If your backend can create a team from a code, this returns the team and links the user.
                teamRepository.joinOrCreateTeam(code)
            }
            teamRepository.setActiveTeam(joined)
            // Sync changes; if it fails, proceed but inform the user and log
            runCatching { teamRepository.sync() }
                .onFailure { e ->
                    Clogger.w(TAG, "Sync failed after joining team, will retry later")
                    _events.send(UiEvent.Toast("Team joined, but sync pending. Check connection."))
                }
            joined
        }.onSuccess { team ->
            Clogger.i(TAG, "Joined team ${team.name} (${team.id}); set active")
            _state.update { it.copy(isBusy = false) }
            _events.send(UiEvent.NavigateHome)
        }.onFailure { e ->
            Clogger.e(TAG, "Failed to join team", e)
            _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Failed to join team") }
            _events.send(UiEvent.Toast("Could not join team: ${e.message ?: "Unknown error"}"))
        }
    }
}

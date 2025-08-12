package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow // NEW
import kotlinx.coroutines.flow.MutableStateFlow  // NEW
import java.time.Instant // NEW
import java.util.UUID    // NEW

@HiltViewModel
class TeamViewerViewModel @Inject constructor(
    private val repo: TeamRepository
) : ViewModel() {

    // Flow of teams the current user belongs to
    val teams: StateFlow<List<Team>> = repo.getTeamsForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- NEW: Lightweight UI signals ----
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> get() = _busy

    private val _toast = MutableSharedFlow<String>()
    val toast: MutableSharedFlow<String> get() = _toast

    // TeamViewerViewModel.kt
    fun syncTeams() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.sync()
                Clogger.i("TeamViewerVM", "sync() completed")
            } catch (e: CancellationException) {
                // user navigated away / scope was cancelled — no crash
                Clogger.i("TeamViewerVM", "sync() was cancelled")
            } catch (e: Throwable) {
                Clogger.e("TeamViewerVM", "sync() failed", e)
            }
        }
    }

    // ---- NEW: Create / Join actions ----
    fun createTeamFromName(teamName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _busy.value = true
                val team = buildTeam(teamName)
                repo.createTeam(team)   // local-first
                repo.sync()             // push+join remotely (your repo handles this)
                _toast.emit("Team created")
            } catch (e: Throwable) {
                Clogger.e("TeamViewerVM", "createTeam failed", e)
                _toast.tryEmit(e.message ?: "Failed to create team")
            } finally {
                _busy.value = false
            }
        }
    }

    fun joinByCode(teamCode: String, userCode: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _busy.value = true
                // Uses your combined repo helper that joins or creates a stub locally.
                val joined = repo.joinOrCreateTeam(teamCode.trim())
                repo.setActiveTeam(joined)
                repo.sync()
                _toast.emit("Joined ${joined.name}")
            } catch (e: Throwable) {
                Clogger.e("TeamViewerVM", "joinByCode failed", e)
                _toast.tryEmit(e.message ?: "Failed to join team")
            } finally {
                _busy.value = false
            }
        }
    }

    // ---- NEW: small helper so we can construct a valid Team quickly ----
    private fun buildTeam(name: String): Team {
        val now = Instant.now()
        val code = generateCode(name)
        return Team(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            code = code,
            name = name,
            alias = code,
            description = "",
            composition = TeamComposition.UNISEX_MALE,
            denomination = TeamDenomination.OPEN,
            yearFormed = now.atZone(java.time.ZoneId.systemDefault()).year.toString(),
            contactCell = "",
            contactMail = "",
            clubAddress = "",
            isActive = true
        )
    }

    private fun generateCode(name: String): String =
        name.uppercase().split(Regex("\\s+"))
            .take(2).joinToString("") { it.take(3) }
            .replace(Regex("[^A-Z0-9]"), "")
            .ifEmpty { "TEAM" }
            .take(6)
}

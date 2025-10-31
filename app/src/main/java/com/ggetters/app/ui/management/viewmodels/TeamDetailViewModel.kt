package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.ui.management.views.TeamDetailActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TeamDetailViewModel @Inject constructor(
    private val repo: TeamRepository,
    private val userRepo: UserRepository,
    private val codeGen: com.ggetters.app.core.utils.CodeGenerationUtils,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val teamId = savedStateHandle.getStateFlow(TeamDetailActivity.EXTRA_TEAM_ID, "")
    val team: StateFlow<Team?> = teamId
        .flatMapLatest { id -> if (id.isBlank()) kotlinx.coroutines.flow.flowOf(null) else repo.getByIdFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val members: StateFlow<List<User>> = userRepo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val memberCount: StateFlow<Int> = members
        .map { it.count() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val coachCount: StateFlow<Int> = members
        .map { users -> users.count { u -> u.role == UserRole.COACH || u.role == UserRole.COACH_PLAYER } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val playerCount: StateFlow<Int> = members
        .map { users -> users.count { u -> u.role == UserRole.FULL_TIME_PLAYER || u.role == UserRole.PART_TIME_PLAYER || u.role == UserRole.COACH_PLAYER } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun save(team: Team) = viewModelScope.launch {
        repo.upsert(team)   // local (marks dirty) :contentReference[oaicite:10]{index=10}
        repo.sync()         // push+merge   :contentReference[oaicite:11]{index=11}
    }

    fun ensureTeamCode(teamId: String) = viewModelScope.launch {
        val current = repo.getById(teamId) ?: return@launch
        val code = current.code?.uppercase()
        val valid = code?.matches(Regex("^[A-Z0-9]{6}$")) == true
        if (!valid) {
            val unique = codeGen.generateCollisionSafeTeamCode()
            repo.updateTeamCode(teamId, unique)
            repo.sync()
        }
    }
}
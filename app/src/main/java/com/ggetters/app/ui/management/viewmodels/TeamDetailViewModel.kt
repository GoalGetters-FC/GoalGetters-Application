package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.ui.management.views.TeamDetailActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val repo: TeamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val teamId = savedStateHandle.getStateFlow(TeamDetailActivity.EXTRA_TEAM_ID, "")
    val team: StateFlow<Team?> = teamId
        .mapLatest { id -> if (id.isBlank()) null else repo.getById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(team: Team) = viewModelScope.launch {
        repo.upsert(team)   // local (marks dirty) :contentReference[oaicite:10]{index=10}
        repo.sync()         // push+merge   :contentReference[oaicite:11]{index=11}
    }
}
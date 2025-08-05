package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewerViewModel @Inject constructor(
    private val teamRepo: TeamRepository
) : ViewModel() {

    // Backing property for teams
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    init {
        // Collect teams from repo and keep StateFlow updated
        viewModelScope.launch {
            teamRepo.all().collect { teams ->
                _teams.value = teams
            }
        }
    }
}

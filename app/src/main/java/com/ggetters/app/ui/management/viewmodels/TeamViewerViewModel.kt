package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
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

@HiltViewModel
class TeamViewerViewModel @Inject constructor(
    private val repo: TeamRepository
) : ViewModel() {

    // Flow of teams the current user belongs to
    val teams: StateFlow<List<Team>> = repo.getTeamsForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Optionally expose more UI state later as needed

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

}

// app/src/main/java/com/ggetters/app/ui/central/viewmodels/ProfileViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val teamRepo: TeamRepository
) : ViewModel() {

    /** App-wide active team (Room source-of-truth). */
    val activeTeam: StateFlow<Team?> =
        teamRepo.getActiveTeam()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() = authService.logout()
}

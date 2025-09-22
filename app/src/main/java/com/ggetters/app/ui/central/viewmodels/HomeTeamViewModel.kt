package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.ui.central.dialogs.InsertUserDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeTeamViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val teamDataService: TeamRepository,
    private val userDataService: UserRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "HomeTeamViewModel"
    }


// --- Fields


    val activeTeam: StateFlow<Team?> =
        teamDataService.getActiveTeam()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    val teamUsers: StateFlow<List<User>> =
        userDataService.all()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


// --- Contracts


    fun insertUser(
        formFields: InsertUserDialog.Result
    ) = viewModelScope.launch {
        val team = activeTeam.value ?: return@launch
        val teamId = team.id
        val now = Instant.now()
        val tempId = UUID.randomUUID().toString()

        // Parse DOB from string
        val dateOfBirth = runCatching {
            if (formFields.dateOfBirth.isNotBlank()) {
                LocalDate.parse(formFields.dateOfBirth, DateTimeFormatter.ISO_DATE)
            } else null
        }.getOrNull()

        val user = User(
            id = tempId,
            authId = tempId,
            teamId = teamId,
            joinedAt = now,
            role = UserRole.FULL_TIME_PLAYER,
            name = formFields.name,
            surname = formFields.surname,
            alias = "",
            dateOfBirth = dateOfBirth,
            email = null,
            position = toUserPosition(formFields.position),
            number = formFields.number.toIntOrNull(),
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        userDataService.upsert(user)
        userDataService.sync()
    }


    private fun toUserPosition(label: String): UserPosition? =
        when (label.trim().lowercase()) {
            "striker" -> UserPosition.STRIKER
            "forward" -> UserPosition.FORWARD
            "midfielder" -> UserPosition.MIDFIELDER
            "defender" -> UserPosition.DEFENDER
            "goalkeeper" -> UserPosition.GOALKEEPER
            "winger" -> UserPosition.WINGER
            "center back" -> UserPosition.CENTER_BACK
            "centre back" -> UserPosition.CENTER_BACK
            "full back" -> UserPosition.FULL_BACK
            else -> null
        }


    fun getCurrentUserAuthId() = authService.getCurrentUser()!!.uid
}
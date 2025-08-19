// app/src/main/java/com/ggetters/app/ui/central/viewmodels/HomePlayersViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomePlayersViewModel @Inject constructor(
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository   // Combined repo
) : ViewModel() {

    /** App-wide active team (Room source-of-truth). */
    val activeTeam: StateFlow<Team?> =
        teamRepo.getActiveTeam()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Team-scoped members from local Room, auto-updated by sync. */
    val players: StateFlow<List<User>> =
        userRepo.all()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Pull remote -> local & push dirty local -> remote for the active team. */
    fun refresh() = viewModelScope.launch { userRepo.sync() }

    /**
     * Creates a roster member for the active team.
     * TODO (Invites): replace generated id with Auth UID after invite/accept flow.
     */
    fun addPlayer(
        firstName: String,
        lastName: String,
        email: String,
        positionLabel: String,
        jerseyNumber: Int?,
        dateOfBirthIso: String?
    ) = viewModelScope.launch {
        val team = activeTeam.value ?: return@launch
        val teamId = team.id

        val position = toUserPosition(positionLabel)
        val dob = dateOfBirthIso?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        val now = Instant.now()
        val tempId = UUID.randomUUID().toString() // TODO (Invites): use auth UID when player joins

        val user = User(
            id = tempId,
            authId = tempId,               // keep same for now (local placeholder)
            teamId = teamId,
            joinedAt = now,
            role = UserRole.FULL_TIME_PLAYER,
            name = firstName,
            surname = lastName,
            alias = "",                    // optional
            dateOfBirth = dob,
            email = email,
            position = position,
            number = jerseyNumber,
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        userRepo.upsert(user)  // writes to Room (marks dirty)
        userRepo.sync()        // push to Firestore & pull latest
    }

    private fun toUserPosition(label: String): UserPosition? =
        when (label.trim().lowercase()) {
            "striker"       -> UserPosition.STRIKER
            "forward"       -> UserPosition.FORWARD
            "midfielder"    -> UserPosition.MIDFIELDER
            "defender"      -> UserPosition.DEFENDER
            "goalkeeper"    -> UserPosition.GOALKEEPER
            "winger"        -> UserPosition.WINGER
            "center back"   -> UserPosition.CENTER_BACK
            "centre back"   -> UserPosition.CENTER_BACK
            "full back"     -> UserPosition.FULL_BACK
            else            -> null
        }
}

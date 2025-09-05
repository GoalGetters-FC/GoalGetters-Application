package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.sync.SyncManager
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TeamViewerViewModel @Inject constructor(
    private val repo: TeamRepository,
    private val userRepo: UserRepository,
    private val syncManager: SyncManager,
    private val eventRepo: EventRepository,
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    // Flow of teams the current user belongs to
    val teams: StateFlow<List<Team>> = repo.getTeamsForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- NEW: Lightweight UI signals ----
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> get() = _busy

    private val _toast = MutableSharedFlow<String>()
    val toast: MutableSharedFlow<String> get() = _toast

    sealed interface DeleteState { object Idle : DeleteState; object Deleting : DeleteState; data class Error(val msg:String): DeleteState }
    private val _delete = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val delete: StateFlow<DeleteState> = _delete

    fun syncTeams() {
        viewModelScope.launch {
            try {
                Clogger.i("Sync", "Manual sync start")
                syncManager.syncAll()
                Clogger.i("Sync", "Manual sync finished")
            } catch (e: Exception) {
                Clogger.e("Sync", "Manual sync failed", e)
            }
        }
    }

    fun deleteTeam(team: Team) = viewModelScope.launch {
        _delete.value = DeleteState.Deleting
        runCatching {
            repo.delete(team); repo.sync()
        }.onSuccess {
            _delete.value = DeleteState.Idle
        }.onFailure {
            _delete.value = DeleteState.Error(it.message ?: "Failed to delete")
        }
    }

    // Create / Join actions
    fun createTeamFromName(teamName: String, authId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _busy.value = true
                val team = buildTeam(teamName)
                repo.createTeam(team)

                // 👇 Seed creator user
                val creator = User(
                    id = authId,
                    authId = authId,
                    teamId = team.id,
                    role = UserRole.COACH,
                    name = "Coach",
                    surname = "Unknown",
                    email = FirebaseAuth.getInstance().currentUser?.email,
                    status = UserStatus.ACTIVE
                )
                userRepo.insertLocal(creator)

                repo.sync()
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

    /** Make this the active team app-wide (local-first), then try to sync. */
    fun switchTo(team: Team) = viewModelScope.launch {
        repo.setActiveTeam(team)
        // optional: mirror to remote immediately
        runCatching { repo.sync() }
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
            yearFormed = now.atZone(ZoneId.systemDefault()).year.toString(),
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

    fun createDebugTeam(authId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _busy.value = true

                val now = Instant.now()

                // 1. Build base team
                val team = buildTeam("Chelsea FC")
                repo.createTeam(team)

                // 2. Insert coach = current Firebase user
                val coach = User(
                    id = authId,
                    authId = authId,
                    teamId = team.id,
                    joinedAt = now,
                    role = UserRole.COACH,
                    name = "Mauricio",
                    surname = "Pochettino",
                    email = FirebaseAuth.getInstance().currentUser?.email,
                    status = UserStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now
                )
                userRepo.upsert(coach)

                // 3. Seed Chelsea players (starters, subs, inactive)
                val chelseaPlayers = listOf(
                    "Robert Sánchez" to "goalkeeper",
                    "Reece James" to "defender",
                    "Thiago Silva" to "defender",
                    "Levi Colwill" to "defender",
                    "Ben Chilwell" to "defender",
                    "Enzo Fernández" to "midfielder",
                    "Moises Caicedo" to "midfielder",
                    "Conor Gallagher" to "midfielder",
                    "Raheem Sterling" to "winger",
                    "Nicolas Jackson" to "striker",
                    "Cole Palmer" to "winger",
                    "Marc Cucurella" to "defender",
                    "Trevoh Chalobah" to "defender",
                    "Noni Madueke" to "winger",
                    "Armando Broja" to "striker",
                    "Mykhailo Mudryk" to "winger",
                    "Wesley Fofana" to "defender (injured)"
                )

                val seededUsers = chelseaPlayers.mapIndexed { idx, (fullName, posLabel) ->
                    val parts = fullName.split(" ", limit = 2)
                    val first = parts.getOrElse(0) { "Player" }
                    val last = parts.getOrElse(1) { "" }

                    User(
                        id = UUID.randomUUID().toString(),
                        authId = "debug_${first.lowercase()}_${idx + 1}",
                        teamId = team.id,
                        joinedAt = now,
                        role = UserRole.FULL_TIME_PLAYER,
                        name = first,
                        surname = last,
                        alias = "",
                        position = toUserPosition(posLabel),
                        number = idx + 1,
                        status = if (posLabel.contains("injured", true)) UserStatus.INJURY else UserStatus.ACTIVE,
                        createdAt = now,
                        updatedAt = now
                    )
                }

                seededUsers.forEach { userRepo.upsert(it) }

                // 4. Seed example events (future)
                val events = listOf(
                    Event(
                        id = UUID.randomUUID().toString(),
                        teamId = team.id,
                        name = "Match vs Arsenal",
                        category = EventCategory.MATCH,
                        location = "Stamford Bridge",
                        startAt = LocalDateTime.ofInstant(now.plusSeconds(86400), ZoneId.systemDefault()),
                        endAt = LocalDateTime.ofInstant(now.plusSeconds(93600), ZoneId.systemDefault()),
                        creatorId = authId,
                        createdAt = now,
                        updatedAt = now,
                        stainedAt = null,
                        description = "Premier League fixture",
                        style = EventStyle.FRIENDLY
                    ),
                    Event(
                        id = UUID.randomUUID().toString(),
                        teamId = team.id,
                        name = "Training Session",
                        category = EventCategory.PRACTICE,
                        location = "Cobham Training Ground",
                        startAt = LocalDateTime.ofInstant(now.plusSeconds(172800), ZoneId.systemDefault()),
                        endAt = LocalDateTime.ofInstant(now.plusSeconds(180000), ZoneId.systemDefault()),
                        creatorId = authId,
                        createdAt = now,
                        updatedAt = now,
                        stainedAt = null,
                        description = "First-team training",
                        style = EventStyle.STANDARD
                    )
                )
                events.forEach { eventRepo.upsert(it) }

                // --- NEW: Completed match in the past (finished, with scorers in description) ---
                val completedStart = LocalDateTime.ofInstant(
                    now.minusSeconds(3 * 24 * 60 * 60), // 3 days ago
                    ZoneId.systemDefault()
                )
                val completedMatch = Event(
                    id = UUID.randomUUID().toString(),
                    teamId = team.id,
                    name = "Match vs Liverpool (Completed)",
                    category = EventCategory.MATCH,
                    location = "Stamford Bridge",
                    startAt = completedStart,
                    endAt = completedStart.plusMinutes(105), // 90' + stoppage
                    creatorId = authId,
                    createdAt = now,
                    updatedAt = now,
                    stainedAt = null,
                    // Store final score + scorers in description for now
                    description = "Full-time: Chelsea 2–1 Liverpool. Scorers: Jackson 23', Palmer 67' — Opponent 78'",
                    style = EventStyle.FRIENDLY
                )
                eventRepo.upsert(completedMatch)

                // 5. Auto-seed attendance for all users for each FUTURE event (default = Not Responded = 3)
                val allUsers = listOf(coach) + seededUsers
                events.forEach { event ->
                    val attendances = allUsers.map { user ->
                        Attendance(
                            eventId = event.id,
                            playerId = user.id,
                            status = 3, // default = Not Responded
                            recordedBy = "system",
                            recordedAt = now,
                            createdAt = now,
                            updatedAt = now
                        )
                    }
                    attendanceRepo.upsertAll(attendances)
                }

                // --- NEW: Attendance for the completed match
                // Coach + first XI marked Present(0), remaining players Absent(1)
                val starters = seededUsers.take(11).map { it.id }.toSet()
                val completedAttendances = allUsers.map { user ->
                    val isStarter = user.id in starters
                    val statusInt = when {
                        user.id == coach.id -> 0 // Present
                        isStarter -> 0           // Present
                        else -> 1                // Absent
                    }
                    Attendance(
                        eventId = completedMatch.id,
                        playerId = user.id,
                        status = statusInt,
                        recordedBy = "system",
                        recordedAt = now,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                attendanceRepo.upsertAll(completedAttendances)

                // 6. Sync repos
                userRepo.sync()
                eventRepo.sync()
                attendanceRepo.sync()
                repo.sync()

                _toast.emit("Chelsea FC debug team seeded ✅")
            } catch (e: Throwable) {
                Clogger.e("TeamViewerVM", "createDebugTeam failed", e)
                _toast.tryEmit("Debug team failed: ${e.message}")
            } finally {
                _busy.value = false
            }
        }
    }

    /** Mapping helper */
    private fun toUserPosition(label: String): UserPosition? =
        when (label.trim().lowercase()) {
            "striker" -> UserPosition.STRIKER
            "forward" -> UserPosition.FORWARD
            "midfielder" -> UserPosition.MIDFIELDER
            "defender" -> UserPosition.DEFENDER
            "goalkeeper" -> UserPosition.GOALKEEPER
            "winger" -> UserPosition.WINGER
            "center back", "centre back" -> UserPosition.CENTER_BACK
            "full back" -> UserPosition.FULL_BACK
            else -> null
        }
}

/**
 * TODO: Fix User insert + sync flow
 *
 * 1. Update UserRepository:
 *    - Add helper:
 *      suspend fun insertAndSync(user: User) {
 *          insertLocal(user)
 *          sync()
 *      }
 *    - Ensure sync() pushes unsynced users from Room → Firestore (dirty flag or fallback: push all).
 *
 * 2. Update TeamViewerViewModel.createTeamFromName:
 *    - Replace:
 *        userRepo.insertLocal(creator)
 *        repo.sync()
 *    - With:
 *        userRepo.insertAndSync(creator)
 *        repo.sync()
 *
 * 3. Check join flow:
 *    - In joinByCode, verify the joining user is also inserted + synced (currently may remain local).
 *
 * 4. Test:
 *    - Create a new team → Firestore users subcollection should contain:
 *        {
 *          "name": "Coach",
 *          "surname": "Unknown",
 *          "role": "COACH",
 *          "status": "ACTIVE"
 *        }
 *    - Verify presence in both local DB and remote.
 *
 * ⚡ Optional cleanup:
 *    - Replace all insertLocal + sync usages with insertAndSync across repos (Users, Events, Lineups, etc.)
 *      for consistency.
 */

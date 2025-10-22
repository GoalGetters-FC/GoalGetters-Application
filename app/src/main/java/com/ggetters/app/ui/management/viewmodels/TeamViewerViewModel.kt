package com.ggetters.app.ui.management.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.sync.SyncManager
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.*
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class TeamViewerViewModel @Inject constructor(
    private val repo: TeamRepository,
    private val userRepo: UserRepository,
    private val syncManager: SyncManager,
    private val eventRepo: EventRepository,
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    // Active user teams
    val teams: StateFlow<List<Team>> = repo.getTeamsForCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- UI state ---
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> get() = _busy

    private val _toast = MutableSharedFlow<String>()
    val toast: SharedFlow<String> get() = _toast

    // --- Sync ---
    fun syncTeams() = viewModelScope.launch {
        try {
            Clogger.i("Sync", "Manual sync start")
            syncManager.syncAll()
            Clogger.i("Sync", "Manual sync finished")
        } catch (e: Exception) {
            Clogger.e("Sync", "Manual sync failed", e)
            _toast.emit("Sync failed: ${e.message}")
        }
    }

    // --- Create Team ---
    fun createTeamFromName(teamName: String, authId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _busy.value = true
            val team = buildTeam(teamName)
            repo.createTeam(team)

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

            // Local insert + sync
            userRepo.insertLocal(creator)
            userRepo.sync()
            repo.sync()

            _toast.emit("Team created")
        } catch (e: Throwable) {
            Clogger.e("TeamViewerVM", "createTeam failed", e)
            _toast.emit("Failed to create team: ${e.message}")
        } finally {
            _busy.value = false
        }
    }

    // --- Join via code ---
    fun joinByCode(teamCode: String, userCode: String?) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _busy.value = true
            val code = teamCode.trim()
            
            // Validate 6-digit alphanumeric code
            if (!code.matches(Regex("^[A-Z0-9]{6}$"))) {
                _toast.emit("Team code must be 6 characters (letters and numbers only).")
                return@launch
            }
            
            val joined = repo.joinOrCreateTeam(code)
            repo.setActiveTeam(joined)
            repo.sync()
            _toast.emit("Joined ${joined.name}")
        } catch (e: Throwable) {
            Clogger.e("TeamViewerVM", "joinByCode failed", e)
            _toast.emit("Failed to join team: ${e.message}")
        } finally {
            _busy.value = false
        }
    }

    // --- Switch Active Team ---
    fun switchTo(team: Team) = viewModelScope.launch {
        repo.setActiveTeam(team)
        runCatching { repo.sync() }
    }

    // --- Leave / Delete Team ---
    fun leaveTeam(team: Team, currentUserId: String) = viewModelScope.launch(Dispatchers.IO) {
        _busy.value = true
        val message = TeamLeaveLogic.attemptLeaveTeam(team, currentUserId, repo, userRepo)
        _toast.emit(message)
        _busy.value = false
    }

    // --- Debug Seeder (Chelsea Example) ---
    fun createDebugTeam(authId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _busy.value = true
            val now = Instant.now()

            // 1. Build base team
            val team = buildTeam("Chelsea FC")
            repo.createTeam(team)

            // 2. Insert coach
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

            // 3. Seed players
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

            // 4. Seed events
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

            // 5. Completed match (past)
            val completedStart = LocalDateTime.ofInstant(now.minusSeconds(259200), ZoneId.systemDefault())
            val completedMatch = Event(
                id = UUID.randomUUID().toString(),
                teamId = team.id,
                name = "Match vs Liverpool (Completed)",
                category = EventCategory.MATCH,
                location = "Stamford Bridge",
                startAt = completedStart,
                endAt = completedStart.plusMinutes(105),
                creatorId = authId,
                createdAt = now,
                updatedAt = now,
                stainedAt = null,
                description = "Full-time: Chelsea 2–1 Liverpool. Scorers: Jackson 23', Palmer 67' — Opponent 78'",
                style = EventStyle.FRIENDLY
            )
            eventRepo.upsert(completedMatch)

            // 6. Attendance seeding
            val allUsers = listOf(coach) + seededUsers
            events.forEach { event ->
                val attendances = allUsers.map { user ->
                    Attendance(
                        eventId = event.id,
                        playerId = user.id,
                        status = 3,
                        recordedBy = "system",
                        recordedAt = now,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                attendanceRepo.upsertAll(attendances)
            }

            val starters = seededUsers.take(11).map { it.id }.toSet()
            val completedAttendances = allUsers.map { user ->
                val statusInt = when {
                    user.id == coach.id -> 0
                    user.id in starters -> 0
                    else -> 1
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

            // 7. Sync
            userRepo.sync()
            eventRepo.sync()
            attendanceRepo.sync()
            repo.sync()

            _toast.emit("Chelsea FC debug team seeded ✅")
        } catch (e: Throwable) {
            Clogger.e("TeamViewerVM", "createDebugTeam failed", e)
            _toast.emit("Debug team failed: ${e.message}")
        } finally {
            _busy.value = false
        }
    }

    // --- Helpers ---
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

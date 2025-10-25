// app/src/main/java/com/ggetters/app/ui/central/viewmodels/EventUpsertViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.core.services.EventNotificationService
import com.ggetters.app.ui.central.models.UpsertState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EventUpsertViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val attendanceRepo: AttendanceRepository,
    private val eventNotificationService: EventNotificationService
    // OPTIONAL: inject an auth provider to get the current UID
    // private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow<UpsertState>(UpsertState.Idle)
    val state: kotlinx.coroutines.flow.StateFlow<UpsertState> = _state

    fun save(
        category: EventCategory,
        title: String,
        location: String?,
        description: String?,
        startAt: Instant?,
        endAt: Instant?,
        meetingAt: Instant? = null, // NOTE: currently unused by Event model
        opponent: String? = null
    ) = viewModelScope.launch {
        val team = teamRepo.getActiveTeam().first()
        if (team == null) {
            _state.value = UpsertState.Error("No active team selected")
            return@launch
        }
        if (startAt == null) {
            _state.value = UpsertState.Error("Pick a start date & time")
            return@launch
        }
        if (endAt != null && endAt.isBefore(startAt)) {
            _state.value = UpsertState.Error("End time is before start time")
            return@launch
        }

        _state.value = UpsertState.Saving

        // Derive opponent if the title starts with "vs ", and build "Home vs Opponent"
        val normalizedTitle = title.trim()
        val explicitOpponent = opponent?.trim().orEmpty()
        val opponentFromTitle = if (category == EventCategory.MATCH && normalizedTitle.lowercase().startsWith("vs ")) {
            normalizedTitle.removePrefix("vs ").trim()
        } else null

        val finalName = if (category == EventCategory.MATCH) {
            val opponentName = when {
                explicitOpponent.isNotBlank() -> explicitOpponent
                opponentFromTitle?.isNotBlank() == true -> opponentFromTitle
                else -> normalizedTitle
            }
            val homeName = team.name.ifBlank { "Home" }
            if (opponentName.isBlank() || opponentName.equals("match", ignoreCase = true) || opponentName.equals("league", ignoreCase = true)) {
                // No opponent provided – fall back to generic name
                "Match"
            } else {
                "$homeName vs $opponentName"
            }
        } else {
            normalizedTitle.ifBlank { defaultTitle(category) }
        }

        // Build event
        val event = Event(
            id = UUID.randomUUID().toString(),
            teamId = team.id,
            name = finalName,
            category = category,
            location = location,
            description = description,
            startAt = LocalDateTime.ofInstant(startAt, ZoneId.systemDefault()),
            endAt = endAt?.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) },
            creatorId = null,
            style = EventStyle.FRIENDLY
        )

        try {
            withContext(Dispatchers.IO) {
                // 1) Save event (local → remote via repo.sync())
                eventRepo.upsert(event)
                eventRepo.sync()

                // 2) Ensure attendance exists for ALL team users (Unknown = 3), idempotent
                ensureAttendanceSeeded(event.id /*, recordedBy = authRepo.currentUidOrNull() */)

                // 3) Pull fresh attendance locally (optional but keeps things consistent)
                attendanceRepo.sync()
            }

            // 4) Create notification for the new event
            try {
                val currentUser = userRepo.getLocalByAuthId(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "")
                currentUser?.let { user ->
                    eventNotificationService.createEventCreatedNotification(
                        event = event,
                        userId = user.id,
                        teamId = team.id
                    )
                }
            } catch (e: Exception) {
                Clogger.e("EventUpsertVM", "Failed to create event notification: ${e.message}", e)
                // Don't fail the entire operation if notification creation fails
            }

            _state.value = UpsertState.Saved(event.id)
            Clogger.i("EventUpsertVM", "Event saved + attendance seeded for team=${team.id}")

        } catch (e: Exception) {
            Clogger.e("EventUpsertVM", "Failed saving event: ${e.message}", e)
            _state.value = UpsertState.Error("Failed to save event: ${e.message}")
        }
    }

    private suspend fun ensureAttendanceSeeded(
        eventId: String,
        recordedBy: String = "system" // replace with auth UID if you inject it
    ) {
        // what users exist already for the event?
        val existing = attendanceRepo.getByEventId(eventId).first()
        val existingIds = existing.map { it.playerId }.toSet()

        // who is on the team?
        val teamUsers = userRepo.all().first() // already scoped to active team in CombinedUserRepository

        // build missing rows only (idempotent)
        val missing = teamUsers
            .filter { it.id !in existingIds }
            .map { u ->
                Attendance(
                    eventId = eventId,
                    playerId = u.id,   // user.id == auth UID
                    status = 3,        // Unknown / Not Responded
                    recordedBy = recordedBy
                )
            }

        if (missing.isNotEmpty()) {
            Clogger.i("EventUpsertVM", "Seeding ${missing.size} attendance rows for event=$eventId")
            attendanceRepo.upsertAll(missing) // local + remote
        } else {
            Clogger.d("EventUpsertVM", "Attendance already seeded for event=$eventId")
        }
    }

    private fun defaultTitle(cat: EventCategory) = when (cat) {
        EventCategory.MATCH -> "Match"
        EventCategory.PRACTICE -> "Practice"
        else -> "Event"
    }
}

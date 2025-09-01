package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.ui.central.models.UpsertState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EventUpsertViewModel @Inject constructor(
    private val repo: EventRepository,
    private val teamRepo: TeamRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UpsertState>(UpsertState.Idle)
    val state: StateFlow<UpsertState> = _state

    fun save(
        category: EventCategory,
        title: String,
        location: String?,
        description: String?,
        startAt: Instant?,
        endAt: Instant?,
        meetingAt: Instant? = null
    ) = viewModelScope.launch {
        // 🔎 Always wait for the active team from DB
        val team = teamRepo.getActiveTeam().first()
        if (team == null) {
            Clogger.e("EventUpsertViewModel", "❌ No active team found, cannot save event")
            _state.value = UpsertState.Error("No active team selected")
            return@launch
        }

        if (startAt == null) {
            Clogger.e("EventUpsertViewModel", "❌ StartAt is null")
            _state.value = UpsertState.Error("Please select a start date and time")
            return@launch
        }
        if (endAt != null && endAt.isBefore(startAt)) {
            Clogger.e("EventUpsertViewModel", "❌ EndAt before StartAt")
            _state.value = UpsertState.Error("End time must be after start time")
            return@launch
        }
        
        if (title.isBlank()) {
            Clogger.e("EventUpsertViewModel", "❌ Title is blank")
            _state.value = UpsertState.Error("Please enter an event title")
            return@launch
        }

        _state.value = UpsertState.Saving

        val event = Event(
            id = UUID.randomUUID().toString(),
            teamId = team.id, // ✅ always from active team
            name = title.ifBlank { defaultTitle(category) },
            category = category,
            location = location,
            description = description,
            startAt = LocalDateTime.ofInstant(startAt, ZoneId.systemDefault()),
            endAt = endAt?.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) },
            creatorId = null,
            style = EventStyle.FRIENDLY
        )

        Clogger.i(
            "EventUpsertViewModel",
            "✅ Saving event '${event.name}' for team='${team.name}' (id=${team.id})"
        )

        repo.upsert(event)
        repo.sync()

        _state.value = UpsertState.Saved(event.id)
        Clogger.i("EventUpsertViewModel", "🎉 Event saved successfully with id=${event.id}")
    }

    private fun defaultTitle(cat: EventCategory) = when (cat) {
        EventCategory.MATCH -> "Match"
        EventCategory.PRACTICE -> "Practice"
        else -> "Event"
    }
}

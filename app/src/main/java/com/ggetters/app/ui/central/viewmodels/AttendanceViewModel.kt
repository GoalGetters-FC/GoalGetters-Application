// app/src/main/java/com/ggetters/app/ui/central/viewmodels/AttendanceViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.AttendanceWithUser
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val userRepo: UserRepository,
    private val teamRepo: TeamRepository
) : ViewModel() {

    private val _players = MutableStateFlow<List<AttendanceWithUser>>(emptyList())
    val players: StateFlow<List<AttendanceWithUser>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPlayers(eventId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Always refresh local from remote for this event
                attendanceRepo.sync()

                // Load attendance rows after sync
                val attendanceRows = attendanceRepo.getByEventId(eventId).first()
                val attendanceMap = attendanceRows.associateBy { it.playerId }

                // Load all users for active team
                val teamUsers = userRepo.all().first()

                // Merge: every user shows, default Unknown(3)
                val merged = teamUsers.map { user ->
                    val attendance = attendanceMap[user.id] ?: Attendance(
                        eventId = eventId,
                        playerId = user.id,               // userId == auth UID
                        status = 3,                       // Unknown
                        recordedBy = "system"
                    )
                    AttendanceWithUser(attendance, user)
                }

                _players.value = merged

            } catch (e: Exception) {
                _error.value = e.message
                Clogger.e("AttendanceVM", "Load failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePlayerStatus(eventId: String, playerId: String, newStatus: Int) {
        viewModelScope.launch {
            try {
                val existing = attendanceRepo.getById(eventId, playerId)
                val updated = existing?.copy(status = newStatus) ?: Attendance(
                    eventId = eventId,
                    playerId = playerId,
                    status = newStatus,
                    recordedBy = "system"
                )
                attendanceRepo.upsert(updated)

                // optimistic local update so UI feels instant
                _players.value = _players.value.map {
                    if (it.user.id == playerId) it.copy(attendance = updated) else it
                }

            } catch (e: Exception) {
                _error.value = e.message
                Clogger.e("AttendanceVM", "Update failed: ${e.message}", e)
            }
        }
    }

    fun clearError() { _error.value = null }
}

// app/src/main/java/com/ggetters/app/ui/central/viewmodels/MatchRosterViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.mappers.RosterMapper
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel for managing match roster (availability + lineup).
 *
 * - Loads Users from UserRepository
 * - Loads RSVPs from AttendanceRepository
 * - Loads Lineup from LineupRepository
 * - Produces a unified list of RosterPlayer for UI
 */
@HiltViewModel
class MatchRosterViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val attendanceRepo: AttendanceRepository,
    private val lineupRepo: LineupRepository
) : ViewModel() {

    private val _players = MutableStateFlow<List<RosterPlayer>>(emptyList())
    val players: StateFlow<List<RosterPlayer>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterStatus = MutableStateFlow<RSVPStatus?>(null)
    val filterStatus: StateFlow<RSVPStatus?> = _filterStatus.asStateFlow()

    /**
     * Load roster (users + RSVPs + lineup).
     */
    fun loadRoster(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val users = userRepo.all().first()
                val attendance = attendanceRepo.getByEventId(matchId).first()
                val lineup = lineupRepo.getByEventId(matchId).first().firstOrNull()
                _players.value = RosterMapper.merge(users, attendance, lineup)

            } catch (e: Exception) {
                _error.value = "Failed to load roster: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Update RSVP for a player.
     */
    fun updatePlayerRSVP(matchId: String, playerId: String, status: RSVPStatus) {
        viewModelScope.launch {
            try {
                attendanceRepo.upsert(
                    Attendance(
                        eventId = matchId,
                        playerId = playerId,
                        status = status.toDbInt(),
                        recordedBy = playerId, // TODO: replace with current coach ID
                        recordedAt = Instant.now()
                    )
                )
                loadRoster(matchId)
            } catch (e: Exception) {
                _error.value = "Failed to update RSVP: ${e.message}"
            }
        }
    }

    private fun RSVPStatus.toDbInt(): Int = when (this) {
        RSVPStatus.AVAILABLE -> 1
        RSVPStatus.MAYBE -> 2
        RSVPStatus.UNAVAILABLE -> 3
        RSVPStatus.NOT_RESPONDED -> 0
    }

    /**
     * Filter players by RSVP status.
     */
    fun filterPlayers(status: RSVPStatus?) {
        _filterStatus.value = status
    }

    /**
     * Get players based on active filter.
     */
    fun getFilteredPlayers(): List<RosterPlayer> {
        val currentFilter = _filterStatus.value
        return if (currentFilter == null) {
            _players.value
        } else {
            _players.value.filter { it.status == currentFilter }
        }
    }

    /**
     * Basic RSVP statistics.
     */
    fun getRSVPStats(): Map<RSVPStatus, Int> {
        val roster = _players.value
        return mapOf(
            RSVPStatus.AVAILABLE to roster.count { it.status == RSVPStatus.AVAILABLE },
            RSVPStatus.MAYBE to roster.count { it.status == RSVPStatus.MAYBE },
            RSVPStatus.UNAVAILABLE to roster.count { it.status == RSVPStatus.UNAVAILABLE },
            RSVPStatus.NOT_RESPONDED to roster.count { it.status == RSVPStatus.NOT_RESPONDED }
        )
    }

    fun canStartMatch(): Boolean {
        val availableCount = _players.value.count { it.status == RSVPStatus.AVAILABLE }
        return availableCount >= 11
    }

    fun clearError() {
        _error.value = null
    }
}

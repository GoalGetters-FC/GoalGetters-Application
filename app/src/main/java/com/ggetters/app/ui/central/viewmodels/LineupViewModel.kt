// app/src/main/java/com/ggetters/app/ui/central/viewmodels/LineupViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.ui.shared.extensions.getFullName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LineupViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val userRepo: UserRepository,
    // Optional right now — wire when you’re ready to persist formations/spots
    private val lineupRepo: LineupRepository
) : ViewModel() {

    private val _players = MutableStateFlow<List<RosterPlayer>>(emptyList())
    val players: StateFlow<List<RosterPlayer>> = _players.asStateFlow()

    private val _formation = MutableStateFlow("4-3-3")
    val formation: StateFlow<String> = _formation.asStateFlow()

    private val _positionedPlayers = MutableStateFlow<Map<String, RosterPlayer?>>(emptyMap())
    val positionedPlayers: StateFlow<Map<String, RosterPlayer?>> = _positionedPlayers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Backing caches
    private var cachedEventId: String = ""
    private var cachedTeamUsers = emptyList<com.ggetters.app.data.model.User>()

    fun loadLineup(eventId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                cachedEventId = eventId

                // 1) Pull team users
                cachedTeamUsers = userRepo.all().first()

                // 2) Pull attendance rows for this event
                val attendanceRows = attendanceRepo.getByEventId(eventId).first()
                val byPlayer = attendanceRows.associateBy { it.playerId }

                // 3) Merge → roster players
                _players.value = cachedTeamUsers.map { u ->
                    val att = byPlayer[u.id]
                    val status = rsvpFromInt(att?.status ?: 3)
                    RosterPlayer(
                        playerId = u.id,
                        playerName = u.getFullName(),      // UI ext; safe + consistent
                        jerseyNumber = 0,                  // If you have u.jerseyNumber, drop it in here
                        position = "—",                    // If you have u.position, drop it in here
                        status = status,
                        profileImageUrl = null
                    )
                }

                // Load saved lineup if it exists
                val savedLineups = lineupRepo.getByEventId(eventId).first()
                val savedLineup = savedLineups.firstOrNull()
                if (savedLineup != null) {
                    _formation.value = savedLineup.formation
                    _positionedPlayers.value = savedLineup.spots.associate { spot ->
                        val player = cachedTeamUsers.find { it.id == spot.userId }
                        if (player != null) {
                            val rsvp = byPlayer[player.id]
                            val status = rsvpFromInt(rsvp?.status ?: 3)
                            val rosterPlayer = RosterPlayer(
                                playerId = player.id,
                                playerName = player.getFullName(),
                                jerseyNumber = spot.number,
                                position = spot.position,
                                status = status,
                                profileImageUrl = null
                            )
                            spot.position to rosterPlayer
                        } else {
                            spot.position to null
                        }
                    }.filterValues { it != null } as Map<String, RosterPlayer>
                }

            } catch (e: Exception) {
                _error.value = e.message
                Clogger.e("LineupVM", "loadLineup failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFormation(newFormation: String) {
        if (newFormation == _formation.value) return
        _formation.value = newFormation
        // reset in-formation placements for now
        _positionedPlayers.value = emptyMap()
        // Persist formation change
        saveCurrentLineup()
    }

    fun positionPlayer(player: RosterPlayer, position: String) {
        val updated = _positionedPlayers.value.toMutableMap()

        // Remove player if already on pitch elsewhere
        updated.entries
            .firstOrNull { it.value?.playerId == player.playerId }
            ?.let { updated[it.key] = null }

        updated[position] = player
        _positionedPlayers.value = updated
        // Persist player positioning
        saveCurrentLineup()
    }

    fun swapPlayers(a: String, b: String) {
        val map = _positionedPlayers.value.toMutableMap()
        val pa = map[a]
        val pb = map[b]
        map[a] = pb
        map[b] = pa
        _positionedPlayers.value = map
        // Persist player swap
        saveCurrentLineup()
    }

    fun removePlayerFromPosition(position: String) {
        val updated = _positionedPlayers.value.toMutableMap()
        updated[position] = null
        _positionedPlayers.value = updated
        // Persist player removal
        saveCurrentLineup()
    }

    /**
     * Add players to THIS EVENT’s roster by creating attendance rows with NOT_RESPONDED(3).
     */
    fun addPlayersToEvent(userIds: List<String>) {
        viewModelScope.launch {
            try {
                val eventId = cachedEventId.takeIf { it.isNotBlank() } ?: return@launch
                userIds.forEach { pid ->
                    val existing = attendanceRepo.getById(eventId, pid)
                    val row = existing?.copy(status = 3) ?: Attendance(
                        eventId = eventId,
                        playerId = pid,
                        status = 3, // NOT_RESPONDED
                        recordedBy = "system"
                    )
                    attendanceRepo.upsert(row)
                }
                // Refresh roster after adding
                loadLineup(eventId)
            } catch (e: Exception) {
                _error.value = "Failed to add players: ${e.message}"
            }
        }
    }

    fun getAddableUsers(): List<com.ggetters.app.data.model.User> {
        val onRosterIds = _players.value.map { it.playerId }.toSet()
        return cachedTeamUsers.filter { it.id !in onRosterIds }
    }

    fun saveLineup(eventId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Build lineup spots from positioned players
                val spots = _positionedPlayers.value.mapNotNull { (position, player) ->
                    player?.let { p ->
                        com.ggetters.app.data.model.LineupSpot(
                            userId = p.playerId,
                            number = p.jerseyNumber,
                            position = position,
                            role = com.ggetters.app.data.model.SpotRole.STARTER
                        )
                    }
                }
                
                // Create or update lineup
                val lineup = com.ggetters.app.data.model.Lineup(
                    eventId = eventId,
                    formation = _formation.value,
                    spots = spots,
                    createdBy = "current_user" // TODO: Get from auth
                )
                
                lineupRepo.upsert(lineup)
            } catch (e: Exception) {
                _error.value = "Failed to save lineup: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isLineupComplete(): Boolean {
        val required = requiredPositionsFor(_formation.value)
        val map = _positionedPlayers.value
        return required.all { pos -> map[pos] != null }
    }

    private fun requiredPositionsFor(formation: String): List<String> = when (formation) {
        "4-3-3"   -> listOf("GK","LB","CB1","CB2","RB","CM1","CM2","CM3","LW","ST","RW")
        "4-4-2"   -> listOf("GK","LB","CB1","CB2","RB","LM","CM1","CM2","RM","ST1","ST2")
        "3-5-2"   -> listOf("GK","CB1","CB2","CB3","LWB","CM1","CM2","CM3","RWB","ST1","ST2")
        "4-2-3-1" -> listOf("GK","LB","CB1","CB2","RB","CDM1","CDM2","LW","CAM","RW","ST")
        "5-3-2"   -> listOf("GK","LB","CB1","CB2","CB3","RB","CM1","CM2","CM3","ST1","ST2")
        else      -> emptyList()
    }

    private fun rsvpFromInt(v: Int): RSVPStatus = when (v) {
        0 -> RSVPStatus.AVAILABLE
        1 -> RSVPStatus.MAYBE
        2 -> RSVPStatus.UNAVAILABLE
        else -> RSVPStatus.NOT_RESPONDED
    }

    fun clearError() { _error.value = null }
    
    /**
     * Helper method to save the current lineup with the cached event ID
     */
    private fun saveCurrentLineup() {
        if (cachedEventId.isNotBlank()) {
            saveLineup(cachedEventId)
        }
    }
}

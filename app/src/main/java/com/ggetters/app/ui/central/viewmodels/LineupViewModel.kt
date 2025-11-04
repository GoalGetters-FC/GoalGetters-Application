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
import com.ggetters.app.data.repository.lineup.LineupLocalEditGuard
import com.ggetters.app.ui.shared.extensions.getFullName
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Job
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
    // Optional right now — wire when you're ready to persist formations/spots
    private val lineupRepo: LineupRepository,
    private val eventRepo: com.ggetters.app.data.repository.event.EventRepository
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
    private var cachedAttendanceByPlayer: Map<String, Attendance> = emptyMap()
    private var cachedLineupId: String? = null
    private var cachedLineupCreatedAt: Instant? = null
    private var cachedLineupCreatedBy: String? = null
    private var lineupJob: Job? = null

    fun loadLineup(eventId: String) {
        cachedEventId = eventId
        lineupJob?.cancel()
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1) Pull team users
                cachedTeamUsers = userRepo.all().first()

                // 2) Pull attendance rows for this event
                val attendanceRows = attendanceRepo.getByEventId(eventId).first()
                val byPlayer = attendanceRows.associateBy { it.playerId }
                cachedAttendanceByPlayer = byPlayer

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

                lineupJob = viewModelScope.launch {
                    lineupRepo.getByEventId(eventId).collect { savedLineups ->
                        Clogger.d("LineupViewModel", "Received lineup Flow emission: savedLineups.size=${savedLineups.size} for event=$eventId")
                        savedLineups.forEachIndexed { index, lineup ->
                            Clogger.d("LineupViewModel", "  Lineup[$index]: id=${lineup.id}, formation=${lineup.formation}, spots.size=${lineup.spots.size}, updatedAt=${lineup.updatedAt}")
                            lineup.spots.forEach { spot ->
                                Clogger.d("LineupViewModel", "    Spot: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
                            }
                        }
                        
                        val savedLineup = savedLineups.firstOrNull()

                        if (savedLineup == null) {
                            // No saved lineup found - initialize empty positions for current formation
                            // This ensures the UI has the correct structure even when no saved lineup exists
                            // Only initialize if _positionedPlayers is completely empty (first load scenario)
                            if (_positionedPlayers.value.isEmpty()) {
                                val requiredPositions = requiredPositionsFor(_formation.value).toSet()
                                val emptyPositionsMap = requiredPositions.associateWith { null as RosterPlayer? }
                                Clogger.d("LineupViewModel", "No saved lineup found for event=$eventId, initializing empty positions for formation=${_formation.value}")
                                _positionedPlayers.value = emptyPositionsMap
                            } else {
                                Clogger.d("LineupViewModel", "No saved lineup found for event=$eventId, preserving current UI state (current positionedPlayers.size=${_positionedPlayers.value.size}, formation=${_formation.value})")
                            }
                            cachedLineupId = null
                            cachedLineupCreatedAt = null
                            cachedLineupCreatedBy = null
                            return@collect
                        }

                        Clogger.d("LineupViewModel", "Processing saved lineup: id=${savedLineup.id}, formation=${savedLineup.formation}, spots.size=${savedLineup.spots.size}")
                        Clogger.d("LineupViewModel", "cachedTeamUsers.size=${cachedTeamUsers.size}, cachedAttendanceByPlayer.size=${cachedAttendanceByPlayer.size}")

                        cachedLineupId = savedLineup.id
                        cachedLineupCreatedAt = savedLineup.createdAt
                        cachedLineupCreatedBy = savedLineup.createdBy

                        // ALWAYS update formation from saved lineup to prevent reset issues
                        // This ensures formation persistence even if ViewModel was recreated
                        if (_formation.value != savedLineup.formation) {
                            Clogger.d("LineupViewModel", "Updating formation from ${_formation.value} to ${savedLineup.formation}")
                            _formation.value = savedLineup.formation
                        } else {
                            Clogger.d("LineupViewModel", "Formation matches saved lineup: ${savedLineup.formation}")
                        }

                        val mapped = mutableMapOf<String, RosterPlayer?>()
                        var matchedCount = 0
                        var notFoundCount = 0
                        
                        savedLineup.spots.forEach { spot ->
                            val player = cachedTeamUsers.find { it.id == spot.userId }
                            if (player != null) {
                                matchedCount++
                                val attendance = cachedAttendanceByPlayer[player.id]
                                val status = rsvpFromInt(attendance?.status ?: 3)
                                mapped[spot.position] = RosterPlayer(
                                    playerId = player.id,
                                    playerName = player.getFullName(),
                                    jerseyNumber = spot.number,
                                    position = spot.position,
                                    status = status,
                                    profileImageUrl = null,
                                    lineupRole = spot.role,
                                    lineupPosition = spot.position
                                )
                                Clogger.d("LineupViewModel", "Mapped spot: position=${spot.position}, player=${player.getFullName()}")
                            } else {
                                notFoundCount++
                                mapped[spot.position] = null
                                Clogger.w("LineupViewModel", "Could not find player for spot: position=${spot.position}, userId=${spot.userId} (cachedTeamUsers.size=${cachedTeamUsers.size})")
                            }
                        }

                        // Also initialize empty positions for the formation to ensure all required positions are in the map
                        val requiredPositions = requiredPositionsFor(savedLineup.formation).toSet()
                        requiredPositions.forEach { position ->
                            if (!mapped.containsKey(position)) {
                                mapped[position] = null
                            }
                        }
                        
                        Clogger.d("LineupViewModel", "Built mapped players: matched=$matchedCount, notFound=$notFoundCount, mapped.size=${mapped.size}")
                        Clogger.d("LineupViewModel", "Current _positionedPlayers.size=${_positionedPlayers.value.size}, new mapped.size=${mapped.size}")
                        Clogger.d("LineupViewModel", "Current _positionedPlayers keys: ${_positionedPlayers.value.keys.sorted().joinToString()}")
                        Clogger.d("LineupViewModel", "New mapped keys: ${mapped.keys.sorted().joinToString()}")

                        // Always update _positionedPlayers when we have a saved lineup with spots
                        // This ensures the UI reflects the saved state, especially after fragment/activity recreation
                        // CRITICAL: Always apply saved lineup data, even if maps appear equal, to prevent data loss
                        if (savedLineup.spots.isNotEmpty()) {
                            Clogger.d("LineupViewModel", "Saved lineup has ${savedLineup.spots.size} spots - always applying to ensure persistence")
                            _positionedPlayers.value = mapped
                        } else {
                            // Even if saved lineup has no spots, we should still update to clear any stale UI state
                            // This handles the case where lineup was cleared/saved empty
                            val mapsEqual = _positionedPlayers.value.size == mapped.size && 
                                           _positionedPlayers.value.keys.all { mapped[it]?.playerId == _positionedPlayers.value[it]?.playerId }
                            
                            if (!mapsEqual) {
                                Clogger.d("LineupViewModel", "Updating _positionedPlayers with ${mapped.size} players (maps not equal, saved lineup has no spots)")
                                _positionedPlayers.value = mapped
                            } else {
                                Clogger.d("LineupViewModel", "_positionedPlayers unchanged (maps are equal and saved lineup has no spots)")
                            }
                        }
                    }
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
        // Do NOT clear positioned players - preserve lineup data when formation changes
        // Only filter out positions that don't exist in the new formation
        val validPositions = requiredPositionsFor(newFormation).toSet()
        val filteredPositions = _positionedPlayers.value.filterKeys { validPositions.contains(it) }
        if (filteredPositions.size != _positionedPlayers.value.size) {
            _positionedPlayers.value = filteredPositions
        }
        // Persist formation change with preserved players
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
     * Bulk update all positioned players at once
     * This is used when auto-positioning players to avoid multiple saves
     */
    fun setPositionedPlayers(players: Map<String, RosterPlayer?>) {
        _positionedPlayers.value = players.toMap()
        // Persist the bulk update
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
                    cachedAttendanceByPlayer = cachedAttendanceByPlayer + (pid to row)
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
                
                // Validate eventId is not blank
                if (eventId.isBlank()) {
                    _error.value = "Cannot save lineup: event ID is missing"
                    Clogger.e("LineupViewModel", "Attempted to save lineup with blank eventId")
                    return@launch
                }
                
                // Validate that the event exists in the database
                val event = eventRepo.getById(eventId)
                if (event == null) {
                    _error.value = "Cannot save lineup: event not found"
                    Clogger.e("LineupViewModel", "Attempted to save lineup for non-existent event=$eventId")
                    return@launch
                }
                
                // Get the current user ID from Firebase Auth
                val currentUserId: String? = runCatching {
                    val authUid = FirebaseAuth.getInstance().currentUser?.uid
                    if (authUid != null) {
                        // Try to get the user from the database using authId
                        // First try getLocalByAuthId (most reliable)
                        val user = runCatching { userRepo.getLocalByAuthId(authUid) }.getOrNull()
                            ?: runCatching { userRepo.getById(authUid) }.getOrNull()
                        user?.id
                    } else null
                }.getOrNull()
                
                // Use cached creator if available, otherwise use current user ID, otherwise null
                // Null is valid because the foreign key constraint allows null for createdBy
                val creator = cachedLineupCreatedBy ?: currentUserId
                
                // Build lineup spots from positioned players
                val positionedPlayersMap = _positionedPlayers.value
                
                // Detailed logging to diagnose why spots might be empty
                Clogger.d("LineupViewModel", "Building lineup: positionedPlayersMap.size=${positionedPlayersMap.size}, formation=${_formation.value}")
                positionedPlayersMap.forEach { (position, player) ->
                    if (player != null) {
                        Clogger.d("LineupViewModel", "  Position $position: player=${player.playerName} (id=${player.playerId}, jersey=${player.jerseyNumber})")
                    } else {
                        Clogger.d("LineupViewModel", "  Position $position: null (empty)")
                    }
                }
                
                val spots = positionedPlayersMap.mapNotNull { (position, player) ->
                    player?.let { p ->
                        com.ggetters.app.data.model.LineupSpot(
                            userId = p.playerId,
                            number = p.jerseyNumber,
                            position = position,
                            role = com.ggetters.app.data.model.SpotRole.STARTER
                        )
                    }
                }
                
                Clogger.d("LineupViewModel", "Converted to spots: spots.size=${spots.size} (from ${positionedPlayersMap.values.count { it != null }} non-null players)")
                spots.forEach { spot ->
                    Clogger.d("LineupViewModel", "  Spot: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
                }
                
                val lineupId = cachedLineupId ?: UUID.randomUUID().toString()
                val createdAt = cachedLineupCreatedAt ?: Instant.now()

                // Create or update lineup with current timestamp
                val lineup = com.ggetters.app.data.model.Lineup(
                    id = lineupId,
                    createdAt = createdAt,
                    updatedAt = Instant.now(), // Always use current time for updates
                    eventId = eventId,
                    createdBy = creator, // Can be null if no user found (foreign key allows null)
                    formation = _formation.value,
                    spots = spots
                )
                
                Clogger.d("LineupViewModel", "Saving lineup: id=${lineup.id}, eventId=${lineup.eventId}, formation=${lineup.formation}, spots.size=${lineup.spots.size}")
                // Mark local edit to guard immediate remote overwrites
                LineupLocalEditGuard.markEdited(eventId)
                
                // Save to both offline and online (CombinedLineupRepository handles this)
                lineupRepo.upsert(lineup)
                
                // Update cache after successful save
                cachedLineupId = lineup.id
                cachedLineupCreatedAt = lineup.createdAt
                cachedLineupCreatedBy = lineup.createdBy
                
                Clogger.d("LineupViewModel", "Lineup saved successfully for event=$eventId: ${spots.size} spots, formation=${_formation.value}, createdBy=${creator ?: "null"}")
            } catch (e: Exception) {
                _error.value = "Failed to save lineup: ${e.message}"
                Clogger.e("LineupViewModel", "Failed to save lineup for event=$eventId: ${e.message}", e)
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
        1 -> RSVPStatus.UNAVAILABLE
        2 -> RSVPStatus.MAYBE
        else -> RSVPStatus.NOT_RESPONDED
    }

    fun clearError() { _error.value = null }
    
    /**
     * Helper method to save the current lineup with the cached event ID
     * Validates that eventId is set before saving
     */
    private fun saveCurrentLineup() {
        if (cachedEventId.isNotBlank()) {
            Clogger.d("LineupViewModel", "saveCurrentLineup called: eventId=$cachedEventId, formation=${_formation.value}, positionedPlayers.size=${_positionedPlayers.value.size}")
            saveLineup(cachedEventId)
        } else {
            Clogger.w("LineupViewModel", "Cannot save lineup: cachedEventId is blank. Call loadLineup() first.")
            _error.value = "Cannot save lineup: match not loaded"
        }
    }
    
    /**
     * Handle player substitution - swap players between pitch and bench
     */
    fun handleSubstitution(playerInId: String, playerOutId: String) {
        viewModelScope.launch {
            try {
                val currentPositions = _positionedPlayers.value.toMutableMap()
                val currentPlayers = _players.value.toMutableList()
                
                // Find the player being subbed out (on pitch)
                val playerOutPosition = currentPositions.entries.find { it.value?.id == playerOutId }?.key
                val playerIn = currentPlayers.find { it.id == playerInId }
                val playerOut = currentPlayers.find { it.id == playerOutId }
                
                if (playerOutPosition != null && playerIn != null && playerOut != null) {
                    // Update player status - mark subbed out player as substituted
                    val updatedPlayers = currentPlayers.map { player ->
                        when {
                            player.id == playerOutId -> player.copy(isSubstituted = true)
                            player.id == playerInId -> player.copy(isSubstituted = false)
                            else -> player
                        }
                    }
                    _players.value = updatedPlayers
                    
                    // Put the new player in the position
                    currentPositions[playerOutPosition] = playerIn
                    _positionedPlayers.value = currentPositions
                    
                    // Persist the changes
                    saveCurrentLineup()
                    
                    Clogger.d("LineupViewModel", "Substitution completed: ${playerIn.playerName} in for ${playerOut.playerName}")
                } else {
                    Clogger.e("LineupViewModel", "Substitution failed: Could not find players or position")
                    _error.value = "Substitution failed: Could not find players or position"
                }
            } catch (e: Exception) {
                Clogger.e("LineupViewModel", "Failed to handle substitution: ${e.message}")
                _error.value = "Failed to handle substitution"
            }
        }
    }
}

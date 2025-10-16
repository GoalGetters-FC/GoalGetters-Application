package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.match.MatchEventRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for match event recording operations.
 * Handles event creation and player data loading.
 */
@HiltViewModel
class MatchEventViewModel @Inject constructor(
    private val matchEventRepository: MatchEventRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _availablePlayers = MutableStateFlow<List<User>>(emptyList())
    val availablePlayers: StateFlow<List<User>> = _availablePlayers.asStateFlow()

    private val _eventRecorded = MutableStateFlow(false)
    val eventRecorded: StateFlow<Boolean> = _eventRecorded.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load available players for a specific match (those who are present/available)
     */
    fun loadAvailablePlayers(matchId: String) {
        viewModelScope.launch {
            try {
                // Get all team users
                val allUsers = userRepository.all().first()
                
                // Get attendance data for this match
                val attendanceData = attendanceRepository.getByEventId(matchId).first()
                val attendanceMap = attendanceData.associateBy { it.playerId }
                
                // Filter to only available players (status = 0)
                val availablePlayers = allUsers.filter { user ->
                    val attendance = attendanceMap[user.id]
                    attendance?.status == 0 // AVAILABLE
                }
                
                _availablePlayers.value = availablePlayers
            } catch (e: Exception) {
                _error.value = "Failed to load players: ${e.message}"
            }
        }
    }

    /**
     * Record a new match event
     */
    fun recordEvent(event: MatchEvent) {
        viewModelScope.launch {
            try {
                _eventRecorded.value = false
                _error.value = null
                
                matchEventRepository.insertEvent(event)
                _eventRecorded.value = true
            } catch (e: Exception) {
                _error.value = "Failed to record event: ${e.message}"
            }
        }
    }

    /**
     * Update an existing match event
     */
    fun updateEvent(event: MatchEvent) {
        viewModelScope.launch {
            try {
                _error.value = null
                matchEventRepository.updateEvent(event)
            } catch (e: Exception) {
                _error.value = "Failed to update event: ${e.message}"
            }
        }
    }

    /**
     * Delete a match event
     */
    fun deleteEvent(event: MatchEvent) {
        viewModelScope.launch {
            try {
                _error.value = null
                matchEventRepository.deleteEvent(event)
            } catch (e: Exception) {
                _error.value = "Failed to delete event: ${e.message}"
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset event recorded state
     */
    fun resetEventRecorded() {
        _eventRecorded.value = false
    }
}

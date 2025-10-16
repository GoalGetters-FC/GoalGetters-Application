package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.repository.match.MatchDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Match Details screen.
 *
 * Exposes:
 * - matchDetails: StateFlow<MatchDetails?>
 * - events: StateFlow<List<MatchEvent>>
 */
@HiltViewModel
class MatchDetailsViewModel @Inject constructor(
    private val matchRepo: MatchDetailsRepository
) : ViewModel() {

    private val _matchDetails = MutableStateFlow<MatchDetails?>(null)
    val matchDetails: StateFlow<MatchDetails?> = _matchDetails.asStateFlow()

    private val _events = MutableStateFlow<List<MatchEvent>>(emptyList())
    val events: StateFlow<List<MatchEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var observeJob: Job? = null

    /**
     * Keeps signature for Fragment compatibility, but only uses matchId.
     */
    fun loadMatchDetails(
        matchId: String,
        @Suppress("UNUSED_PARAMETER") matchTitle: String?,
        @Suppress("UNUSED_PARAMETER") homeTeam: String?,
        @Suppress("UNUSED_PARAMETER") awayTeam: String?,
        @Suppress("UNUSED_PARAMETER") venue: String?,
        @Suppress("UNUSED_PARAMETER") matchDateMillis: Long
    ) {
        observeMatch(matchId)
    }

    private fun observeMatch(matchId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                launch {
                    matchRepo.matchDetailsFlow(matchId).collect { _matchDetails.value = it }
                }
                launch {
                    matchRepo.eventsFlow(matchId).collect { _events.value = it }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load match: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePlayerRSVP(playerId: String, status: RSVPStatus) {
        val id = _matchDetails.value?.matchId ?: return
        viewModelScope.launch {
            runCatching { matchRepo.setRSVP(id, playerId, status) }
                .onFailure { _error.value = "Failed to update RSVP: ${it.message}" }
        }
    }

    fun addTimelineEvent(event: MatchEvent) {
        viewModelScope.launch {
            try {
                // Add the event to the timeline
                // Score will be automatically updated via the repository's matchDetailsFlow
                matchRepo.addEvent(event)
                
                // Handle substitutions by updating lineup
                if (event.eventType == com.ggetters.app.data.model.MatchEventType.SUBSTITUTION) {
                    android.util.Log.d("MatchDetailsViewModel", "Processing substitution event: ${event.details}")
                    handleSubstitutionEvent(event)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add event: ${e.message}"
            }
        }
    }
    
    private suspend fun handleSubstitutionEvent(event: MatchEvent) {
        try {
            val playerInId = event.details["substituteIn"] as? String
            val playerOutId = event.details["substituteOut"] as? String
            
            if (playerInId != null && playerOutId != null) {
                // Update attendance status to reflect substitution
                // Mark the subbed-out player as unavailable (substituted)
                matchRepo.setRSVP(event.matchId, playerOutId, com.ggetters.app.data.model.RSVPStatus.UNAVAILABLE)
                
                // Ensure the subbed-in player is available
                matchRepo.setRSVP(event.matchId, playerInId, com.ggetters.app.data.model.RSVPStatus.AVAILABLE)
                
                android.util.Log.d("MatchDetailsViewModel", "Substitution processed: $playerInId in for $playerOutId")
            }
        } catch (e: Exception) {
            android.util.Log.e("MatchDetailsViewModel", "Failed to handle substitution event: ${e.message}")
            _error.value = "Failed to process substitution: ${e.message}"
        }
    }

    fun refreshMatchData() {
        _matchDetails.value?.matchId?.let { observeMatch(it) }
    }

    fun canStartMatch(): Boolean = _matchDetails.value?.canStartMatch() ?: false
    fun getAvailablePlayersCount(): Int = _matchDetails.value?.rsvpStats?.available ?: 0
    fun clearError() { _error.value = null }
}

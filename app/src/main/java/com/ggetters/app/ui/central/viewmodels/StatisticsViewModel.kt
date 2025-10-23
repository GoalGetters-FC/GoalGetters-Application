package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.PlayerStatistics
import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.core.services.StatisticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userRepository: CombinedUserRepository,
    private val statisticsService: StatisticsService
) : ViewModel() {

    private val _player = MutableLiveData<User?>()
    val player: LiveData<User?> = _player

    private val _statistics = MutableLiveData<PlayerStatistics?>()
    val statistics: LiveData<PlayerStatistics?> = _statistics

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPlayerStatistics(playerId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                // Load player data
                val player = userRepository.getById(playerId)
                _player.value = player

                // Load real-time player statistics
                val stats = statisticsService.getPlayerStatistics(playerId)
                _statistics.value = stats

            } catch (e: Exception) {
                _error.value = "Failed to load statistics: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Observes real-time statistics updates
     */
    fun observePlayerStatistics(playerId: String) {
        viewModelScope.launch {
            try {
                statisticsService.getPlayerStatisticsFlow(playerId).collect { stats ->
                    _statistics.value = stats
                }
            } catch (e: Exception) {
                _error.value = "Failed to observe statistics: ${e.message}"
            }
        }
    }

    /**
     * Recalculates statistics for a player
     */
    fun recalculateStatistics(playerId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                
                // Get the player's team to recalculate all team statistics
                val player = userRepository.getById(playerId)
                player?.let { p ->
                    // This would need to be implemented to get team ID from player
                    // For now, we'll just recalculate for this player
                    statisticsService.recalculateAllPlayerStatistics("") // Would need team ID
                }
                
                // Reload statistics
                val stats = statisticsService.getPlayerStatistics(playerId)
                _statistics.value = stats
                
            } catch (e: Exception) {
                _error.value = "Failed to recalculate statistics: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}

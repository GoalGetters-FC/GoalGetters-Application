package com.ggetters.app.core.services

import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.PlayerStatistics
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.data.repository.attendance.CombinedAttendanceRepository
import com.ggetters.app.data.repository.match.CombinedMatchEventRepository
import com.ggetters.app.data.repository.event.CombinedEventRepository
import com.ggetters.app.data.local.dao.PlayerStatisticsDao
import com.ggetters.app.core.utils.Clogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing real-time player statistics updates.
 * Automatically updates player statistics when match events occur.
 */
@Singleton
class StatisticsService @Inject constructor(
    private val userRepository: CombinedUserRepository,
    private val attendanceRepository: CombinedAttendanceRepository,
    private val matchEventRepository: CombinedMatchEventRepository,
    private val eventRepository: CombinedEventRepository,
    private val statisticsDao: PlayerStatisticsDao
) {

    /**
     * Updates player statistics when a match event is recorded
     */
    suspend fun updateStatisticsFromMatchEvent(matchEvent: MatchEvent) {
        try {
            Clogger.d("StatisticsService", "Updating statistics for match event: ${matchEvent.eventType}")
            
            when (matchEvent.eventType) {
                MatchEventType.GOAL -> updateGoalStatistics(matchEvent)
                MatchEventType.YELLOW_CARD -> updateCardStatistics(matchEvent, isRed = false)
                MatchEventType.RED_CARD -> updateCardStatistics(matchEvent, isRed = true)
                MatchEventType.SUBSTITUTION -> updateSubstitutionStatistics(matchEvent)
                else -> {
                    // Other events don't directly affect statistics
                    Clogger.d("StatisticsService", "Event type ${matchEvent.eventType} doesn't affect statistics")
                }
            }
            
            // Update match count for all players in the match
            updateMatchCount(matchEvent.matchId)
            
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to update statistics from match event", e)
        }
    }

    /**
     * Updates attendance statistics when attendance is recorded
     */
    suspend fun updateAttendanceStatistics(eventId: String, attendance: Attendance) {
        try {
            Clogger.d("StatisticsService", "Updating attendance statistics for player ${attendance.playerId}")
            
            val playerId = attendance.playerId
            val currentStats = statisticsDao.getByPlayerId(playerId)
            
            val updatedStats = when (attendance.status) {
                0 -> currentStats?.copy(attended = currentStats.attended + 1) // Available
                1 -> currentStats?.copy(missed = currentStats.missed + 1) // Maybe
                2 -> currentStats?.copy(missed = currentStats.missed + 1) // Unavailable
                else -> currentStats
            }
            
            // Update scheduled count
            val finalStats = updatedStats?.copy(scheduled = updatedStats.scheduled + 1)
            
            if (finalStats != null) {
                statisticsDao.upsert(finalStats)
                Clogger.d("StatisticsService", "Updated attendance stats for player $playerId")
            }
            
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to update attendance statistics", e)
        }
    }

    /**
     * Gets real-time player statistics
     */
    suspend fun getPlayerStatistics(playerId: String): PlayerStatistics? {
        return try {
            statisticsDao.getByPlayerId(playerId)
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to get player statistics", e)
            null
        }
    }

    /**
     * Gets real-time player statistics as Flow for reactive updates
     */
    fun getPlayerStatisticsFlow(playerId: String): Flow<PlayerStatistics?> {
        return statisticsDao.getByPlayerIdFlow(playerId)
    }

    /**
     * Calculates and updates statistics for all players in a team
     */
    suspend fun recalculateAllPlayerStatistics(teamId: String) {
        try {
            Clogger.d("StatisticsService", "Recalculating statistics for team $teamId")
            
            // For now, we'll use a simplified approach
            // In a real implementation, you would get team players and calculate their stats
            Clogger.d("StatisticsService", "Statistics recalculation requested for team $teamId")
            
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to recalculate player statistics", e)
        }
    }

    private suspend fun updateGoalStatistics(matchEvent: MatchEvent) {
        val playerId = matchEvent.playerId ?: return
        val currentStats = statisticsDao.getByPlayerId(playerId)
        val updatedStats = currentStats?.copy(goals = currentStats.goals + 1)
        
        if (updatedStats != null) {
            statisticsDao.upsert(updatedStats)
            Clogger.d("StatisticsService", "Updated goal count for player $playerId")
        }
    }

    private suspend fun updateCardStatistics(matchEvent: MatchEvent, isRed: Boolean) {
        val playerId = matchEvent.playerId ?: return
        val currentStats = statisticsDao.getByPlayerId(playerId)
        
        val updatedStats = if (isRed) {
            currentStats?.copy(redCards = currentStats.redCards + 1)
        } else {
            currentStats?.copy(yellowCards = currentStats.yellowCards + 1)
        }
        
        if (updatedStats != null) {
            statisticsDao.upsert(updatedStats)
            Clogger.d("StatisticsService", "Updated card count for player $playerId")
        }
    }

    private suspend fun updateSubstitutionStatistics(matchEvent: MatchEvent) {
        // Substitutions don't directly affect statistics, but we might want to track minutes played
        Clogger.d("StatisticsService", "Substitution recorded for match ${matchEvent.matchId}")
    }

    private suspend fun updateMatchCount(matchId: String) {
        // Get all players who participated in this match
        val matchEvents = matchEventRepository.getEventsByMatchId(matchId).first()
        val playerIds = matchEvents.mapNotNull { it.playerId }.distinct()
        
        for (playerId in playerIds) {
            val currentStats = statisticsDao.getByPlayerId(playerId)
            val updatedStats = currentStats?.copy(matches = currentStats.matches + 1)
            
            if (updatedStats != null) {
                statisticsDao.upsert(updatedStats)
            }
        }
    }

    private suspend fun calculatePlayerStatistics(
        playerId: String,
        events: List<com.ggetters.app.data.model.Event>,
        attendances: List<Attendance>
    ): PlayerStatistics {
        // Calculate attendance statistics
        val playerAttendances = attendances.filter { it.playerId == playerId }
        val scheduled = playerAttendances.size
        val attended = playerAttendances.count { it.status == 0 }
        val missed = playerAttendances.count { it.status in listOf(1, 2) }
        
        // Calculate match statistics from match events
        // For now, we'll use simplified calculations
        val goals = 0
        val assists = 0
        val yellowCards = 0
        val redCards = 0
        val matchesPlayed = 0
        
        // Calculate minutes played (simplified - would need more complex logic for actual minutes)
        val minutesPlayed = matchesPlayed * 90 // Assume 90 minutes per match
        
        return PlayerStatistics(
            playerId = playerId,
            scheduled = scheduled,
            attended = attended,
            missed = missed,
            goals = goals,
            assists = assists,
            matches = matchesPlayed,
            yellowCards = yellowCards,
            redCards = redCards,
            cleanSheets = 0, // Would need goalkeeper-specific logic
            weight = 0.0, // Would need to be set separately
            minutesPlayed = minutesPlayed
        )
    }
}

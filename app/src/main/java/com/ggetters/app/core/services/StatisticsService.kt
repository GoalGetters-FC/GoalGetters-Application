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
            
            // Get all users in the team - we'll filter to players only
            val users = userRepository.all().first()
            val players = users.filter { it.teamId == teamId }
            
            players.forEach { player ->
                recalculatePlayerStatistics(player.id, teamId)
            }
            
            Clogger.d("StatisticsService", "Recalculated statistics for ${players.size} players")
            
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to recalculate player statistics", e)
        }
    }
    
    /**
     * Recalculates statistics for a specific player
     */
    suspend fun recalculatePlayerStatistics(playerId: String, teamId: String) {
        try {
            Clogger.d("StatisticsService", "Recalculating statistics for player $playerId")
            
            // Get all attendances for this player
            val attendances = attendanceRepository.getByUserId(playerId).first()
            
            // Get all match events for this player
            val allMatchEvents = mutableListOf<MatchEvent>()
            val events = eventRepository.getByTeamId(teamId).first()
            for (event in events) {
                if (event.category == com.ggetters.app.data.model.EventCategory.MATCH) {
                    val matchEvents = matchEventRepository.getEventsByMatchId(event.id).first()
                    allMatchEvents.addAll(matchEvents.filter { it.playerId == playerId })
                }
            }
            
            // Calculate statistics
            val stats = calculatePlayerStatisticsFromData(playerId, attendances, allMatchEvents)
            
            // Save to database
            statisticsDao.upsert(stats)
            
            Clogger.d("StatisticsService", "Recalculated stats for player $playerId: $stats")
            
        } catch (e: Exception) {
            Clogger.e("StatisticsService", "Failed to recalculate player statistics for $playerId", e)
        }
    }
    
    /**
     * Ensures a player has a statistics record, creating one if needed
     */
    suspend fun ensurePlayerStatistics(playerId: String, teamId: String) {
        val existing = statisticsDao.getByPlayerId(playerId)
        if (existing == null) {
            // Create initial empty statistics
            val emptyStats = PlayerStatistics(playerId = playerId)
            statisticsDao.upsert(emptyStats)
            Clogger.d("StatisticsService", "Created initial statistics for player $playerId")
            
            // Recalculate from existing data
            recalculatePlayerStatistics(playerId, teamId)
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

    /**
     * Calculates player statistics from raw data
     */
    private suspend fun calculatePlayerStatisticsFromData(
        playerId: String,
        attendances: List<Attendance>,
        matchEvents: List<MatchEvent>
    ): PlayerStatistics {
        // Calculate attendance statistics
        val scheduled = attendances.size
        val attended = attendances.count { it.status == 0 } // 0 = Present
        val missed = attendances.count { it.status in listOf(1, 2) } // 1 = Absent, 2 = Late
        
        // Calculate match statistics from match events
        val goals = matchEvents.count { it.eventType == MatchEventType.GOAL }
        val yellowCards = matchEvents.count { it.eventType == MatchEventType.YELLOW_CARD }
        val redCards = matchEvents.count { it.eventType == MatchEventType.RED_CARD }
        
        // Count assists from goal event details
        val assists = matchEvents.count { event ->
            event.eventType == MatchEventType.GOAL && 
            event.details["assistId"] == playerId
        }
        
        // Count unique matches where player had events
        val matchesPlayed = matchEvents.map { it.matchId }.distinct().size
        
        // Calculate minutes played (simplified - assume 90 minutes per match unless substituted)
        val substitutionEvents = matchEvents.filter { 
            it.eventType == MatchEventType.SUBSTITUTION 
        }
        
        // For simplicity, assume full match unless subbed off
        val minutesPlayed = matchesPlayed * 90 // Could be enhanced with substitution timing
        
        // Get existing weight if available (preserve it during recalculation)
        val existingWeight = statisticsDao.getByPlayerId(playerId)?.weight ?: 0.0
        
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
            cleanSheets = 0, // Would need goalkeeper-specific logic and opponent goals tracking
            weight = existingWeight, // Preserve existing weight
            minutesPlayed = minutesPlayed
        )
    }
}

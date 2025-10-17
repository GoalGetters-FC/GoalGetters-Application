package com.ggetters.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType

/**
 * Room entity for MatchEvent.
 * Represents match events like goals, cards, substitutions, etc.
 */
@Entity(
    tableName = "match_events",
    indices = [
        Index(value = ["matchId"]),
        Index(value = ["eventType"]),
        Index(value = ["playerId"]),
        Index(value = ["minute"])
    ]
)
data class MatchEventEntity(
    @PrimaryKey
    val id: String,
    val matchId: String,
    val eventType: String, // MatchEventType enum as string
    val timestamp: Long,
    val minute: Int,
    val playerId: String? = null,
    val playerName: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val details: String? = null, // JSON string for additional details
    val createdBy: String,
    val isConfirmed: Boolean = true
) {
    /**
     * Convert to domain model
     */
    fun toDomainModel(): MatchEvent {
        return MatchEvent(
            id = id,
            matchId = matchId,
            eventType = try {
                MatchEventType.valueOf(eventType)
            } catch (e: IllegalArgumentException) {
                MatchEventType.SCORE_UPDATE // fallback
            },
            timestamp = timestamp,
            minute = minute,
            playerId = playerId,
            playerName = playerName,
            teamId = teamId,
            teamName = teamName,
            details = emptyMap(), // TODO: parse JSON details if needed
            createdBy = createdBy,
            isConfirmed = isConfirmed
        )
    }
    
    companion object {
        /**
         * Create entity from domain model
         */
        fun fromDomainModel(event: MatchEvent): MatchEventEntity {
            return MatchEventEntity(
                id = event.id,
                matchId = event.matchId,
                eventType = event.eventType.name,
                timestamp = event.timestamp,
                minute = event.minute,
                playerId = event.playerId,
                playerName = event.playerName,
                teamId = event.teamId,
                teamName = event.teamName,
                details = null, // TODO: serialize details to JSON if needed
                createdBy = event.createdBy,
                isConfirmed = event.isConfirmed
            )
        }
    }
}

package com.ggetters.app.data.repository.match

import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.RSVPStatus
import kotlinx.coroutines.flow.Flow

interface MatchDetailsRepository {
    fun matchDetailsFlow(matchId: String): Flow<MatchDetails>
    fun eventsFlow(matchId: String): Flow<List<MatchEvent>>
    suspend fun setRSVP(matchId: String, playerId: String, status: RSVPStatus)
    suspend fun addEvent(event: MatchEvent)
}

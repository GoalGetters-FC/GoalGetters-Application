package com.ggetters.app.data.repository.match

import com.ggetters.app.data.local.dao.MatchEventDao
import com.ggetters.app.data.local.entity.MatchEventEntity
import com.ggetters.app.data.model.MatchEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline implementation of MatchEventRepository.
 * Handles local database operations for match events.
 */
@Singleton
class OfflineMatchEventRepository @Inject constructor(
    private val matchEventDao: MatchEventDao
) : MatchEventRepository {
    
    override fun getEventsByMatchId(matchId: String): Flow<List<MatchEvent>> {
        return matchEventDao.getEventsByMatchId(matchId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEvent>> {
        return matchEventDao.getEventsByMatchIdAndType(matchId, eventType)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override suspend fun getEventById(eventId: String): MatchEvent? {
        return matchEventDao.getEventById(eventId)?.toDomainModel()
    }
    
    override suspend fun insertEvent(event: MatchEvent) {
        matchEventDao.insert(MatchEventEntity.fromDomainModel(event))
    }
    
    override suspend fun updateEvent(event: MatchEvent) {
        matchEventDao.update(MatchEventEntity.fromDomainModel(event))
    }
    
    override suspend fun deleteEvent(event: MatchEvent) {
        matchEventDao.delete(MatchEventEntity.fromDomainModel(event))
    }
    
    override suspend fun deleteEventsByMatchId(matchId: String) {
        matchEventDao.deleteEventsByMatchId(matchId)
    }
    
    override suspend fun deleteEventById(eventId: String) {
        matchEventDao.deleteEventById(eventId)
    }
    
    override suspend fun getEventCountByMatchId(matchId: String): Int {
        return matchEventDao.getEventCountByMatchId(matchId)
    }
}

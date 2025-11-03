package com.ggetters.app.data.repository.lineup

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.local.dao.LineupDao
import com.ggetters.app.data.model.Lineup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class OfflineLineupRepository @Inject constructor(
    private val dao: LineupDao
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = dao.getAll()

    override suspend fun getById(id: String): Lineup? {
        return runBlocking {
            dao.getById(id)
        }
    }

    override suspend fun upsert(entity: Lineup) {
        Clogger.d("OfflineLineupRepository", "Upserting lineup to offline DB: id=${entity.id}, eventId=${entity.eventId}, formation=${entity.formation}, spots.size=${entity.spots.size}")
        entity.spots.forEach { spot ->
            Clogger.d("OfflineLineupRepository", "  Spot being saved: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
        }
        dao.save(entity)
        Clogger.d("OfflineLineupRepository", "Lineup saved to offline DB: ${entity.id}")
    }

    override suspend fun delete(entity: Lineup) {
        dao.delete(entity)
    }

    suspend fun replaceForEvent(eventId: String, lineups: List<Lineup>) {
        Clogger.d("OfflineLineupRepository", "replaceForEvent: eventId=$eventId, replacing with ${lineups.size} lineups")
        lineups.forEachIndexed { index, lineup ->
            Clogger.d("OfflineLineupRepository", "  Replacing with Lineup[$index]: id=${lineup.id}, formation=${lineup.formation}, spots.size=${lineup.spots.size}")
            lineup.spots.forEach { spot ->
                Clogger.d("OfflineLineupRepository", "    Replacement Spot: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
            }
        }
        dao.replaceForEventTransactional(eventId, lineups)
        if (lineups.isNotEmpty()) {
            Clogger.d("OfflineLineupRepository", "Replaced lineups for event=$eventId")
        } else {
            Clogger.w("OfflineLineupRepository", "Replaced lineups for event=$eventId with empty list (all lineups deleted)")
        }
    }

    override suspend fun deleteAll() {
        runBlocking {
            dao.deleteAll()
        }
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> =
        dao.getByEventId(eventId).map { lineups ->
            Clogger.d("OfflineLineupRepository", "Retrieved lineups from offline DB for event=$eventId: lineups.size=${lineups.size}")
            lineups.forEachIndexed { index, lineup ->
                Clogger.d("OfflineLineupRepository", "  Offline DB Lineup[$index]: id=${lineup.id}, formation=${lineup.formation}, spots.size=${lineup.spots.size}")
                lineup.spots.forEach { spot ->
                    Clogger.d("OfflineLineupRepository", "    Offline DB Spot: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
                }
            }
            lineups
        }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override fun sync() {
        TODO("Not yet implemented")
    }
}



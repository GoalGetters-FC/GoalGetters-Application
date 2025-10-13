package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.model.Lineup
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CombinedLineupRepository @Inject constructor(
    private val offline: OfflineLineupRepository,
    private val online: OnlineLineupRepository
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = offline.all()

    override suspend fun getById(id: String): Lineup? {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_getById")
        trace.start()
        try {
            val result = offline.getById(id) ?: online.getById(id)
            if (result != null) trace.putMetric("lineup_found", 1) else trace.putMetric("lineup_found", 0)
            return result
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsert(entity: Lineup) {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_upsert")
        trace.start()
        try {
            offline.upsert(entity)
            online.upsert(entity)
            trace.putMetric("lineup_upserted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun delete(entity: Lineup) {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_delete")
        trace.start()
        try {
            offline.delete(entity)
            online.delete(entity)
            trace.putMetric("lineup_deleted", 1)
        } finally {
            trace.stop()
        }
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> =
        offline.getByEventId(eventId)

    override fun hydrateForTeam(id: String) {
        // TODO: implement if you want cross-layer hydration
        // Wrap in a trace once implemented
    }

    override fun sync() {
        // TODO: implement WorkManager-driven sync if needed here
        // Add a Firebase trace here when you build out sync logic
    }

    override suspend fun deleteAll() {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_deleteAll")
        trace.start()
        try {
            offline.deleteAll()
            online.deleteAll()
            trace.putMetric("lineups_deleted_all", 1)
        } finally {
            trace.stop()
        }
    }
}

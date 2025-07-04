package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates between offline (Room-based) and online (Firestore-based) implementations of [TeamRepository].
 *
 * - Reads always from the local cache for instant/offline access.
 * - Writes to local first, then attempts remote writes.
 * - Provides a [sync] method to pull remote data into local storage.
 */
@Singleton
class CombinedTeamRepository @Inject constructor(
    private val offline: TeamRepository,
    private val online : TeamRepository
) : TeamRepository {

    /**
     * Observe all teams from the local cache.
     *
     * @return a [Flow] that emits the current list of [Team] objects whenever the database changes.
     */
    override fun observeAll(): Flow<List<Team>> =
        offline.observeAll()

    /**
     * Fetch a single [Team] by its UUID from the local cache.
     *
     * @param id the UUID of the team to retrieve.
     * @return the matching [Team] or `null` if not found.
     */
    override suspend fun getById(id: UUID): Team? =
        offline.getById(id)

    /**
     * Save or update a [Team].
     *
     * This writes to the local cache immediately, then attempts to write remotely.
     * Any remote failures are caught and logged but do not interrupt the local write.
     *
     * @param team the [Team] object to save.
     */
    override suspend fun save(team: Team) {
        // Local write always succeeds
        offline.save(team)

        // Attempt remote write, log on failure
        try {
            online.save(team)
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to save team remotely", e)
        }
    }

    /**
     * Delete a [Team] by its UUID.
     *
     * This deletes from the local cache first, then attempts to delete remotely.
     * Any remote failures are caught and logged.
     *
     * @param id the UUID of the team to delete.
     */
    override suspend fun delete(id: UUID) {
        // Local delete
        offline.delete(id)

        // Attempt remote delete, log on failure
        try {
            online.delete(id)
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to delete team remotely", e)
        }
    }

    /**
     * Synchronize local cache with remote data.
     *
     * Fetches all teams from the remote source once and upserts them into the local cache.
     * Any errors during fetch or upsert are caught and logged.
     */
    override suspend fun sync() {
        try {
            // Fetch remote snapshot
            val remoteList = online.observeAll().first()
            // Upsert each remote item into local cache
            remoteList.forEach { offline.save(it) }
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to sync teams", e)
        }
    }
}

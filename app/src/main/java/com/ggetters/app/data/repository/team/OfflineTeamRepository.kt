// app/src/main/java/com/ggetters/app/data/repository/team/OfflineTeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed (offline) implementation of [TeamRepository].
 *
 * All operations read from and write to the local SQLite cache via [TeamDao].
 * Does not interact with the network or remote data source.
 */
@Singleton
class OfflineTeamRepository @Inject constructor(
    private val dao: TeamDao
) : TeamRepository {

    /**
     * Observe all teams from the local database.
     *
     * @return a [Flow] emitting the list of all [Team] objects whenever the table changes.
     */
    override fun observeAll(): Flow<List<Team>> =
        dao.getAll()

    /**
     * Fetch a single [Team] by its UUID from the local database.
     *
     * @param id the UUID of the team to retrieve.
     * @return the matching [Team], or `null` if not found.
     */
    override suspend fun getById(id: UUID): Team? =
        dao.getById(id.toString()).firstOrNull()

    /**
     * Insert or update a [Team] in the local database.
     *
     * On conflict (same primary key), the existing record is replaced.
     *
     * @param team the [Team] object to save.
     */
    override suspend fun save(team: Team): Unit =
        dao.upsert(team)

    /**
     * Delete a [Team] from the local database by its UUID.
     *
     * @param id the UUID of the team to delete.
     */
    override suspend fun delete(id: UUID): Unit =
        dao.deleteById(id.toString())

    /**
     * No-op for offline repository.
     * This method exists to satisfy the [TeamRepository] contract.
     */
    override suspend fun sync(): Unit {
        // Intentionally left blank: sync handled by CombinedTeamRepository
    }
}

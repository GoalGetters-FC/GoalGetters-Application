// app/src/main/java/com/ggetters/app/data/repository/team/OnlineTeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.remote.firestore.TeamFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed (online) implementation of [TeamRepository].
 *
 * Performs CRUD operations against the remote Firestore "teams" collection
 * via [TeamFirestore]. This repository does not interact with the local cache.
 */
@Singleton
class OnlineTeamRepository @Inject constructor(
    private val fs: TeamFirestore
) : TeamRepository {

    /**
     * Observe all teams from Firestore in real time.
     *
     * @return a [Flow] emitting the current list of [Team] objects whenever
     * the remote collection changes.
     */
    override fun observeAll(): Flow<List<Team>> =
        fs.observeAllTeams()

    /**
     * Fetch a single [Team] by its UUID from Firestore.
     *
     * @param id the UUID of the team document to retrieve.
     * @return the matching [Team], or `null` if not found.
     */
    override suspend fun getById(id: UUID): Team? =
        fs.fetchTeam(id.toString())

    /**
     * Save or overwrite a [Team] in Firestore.
     *
     * @param team the [Team] object to save remotely.
     */
    override suspend fun save(team: Team): Unit =
        fs.saveTeam(team)

    /**
     * Delete a [Team] document in Firestore by its UUID.
     *
     * @param id the UUID of the team document to delete.
     */
    override suspend fun delete(id: UUID): Unit =
        fs.deleteTeam(id.toString())

    /**
     * No-op for online repository.
     * This method exists to satisfy the [TeamRepository] contract.
     */
    override suspend fun sync(): Unit {
        // Sync handled by CombinedTeamRepository when orchestrating offline/online
    }
}

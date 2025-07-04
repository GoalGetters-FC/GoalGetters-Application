// app/src/main/java/com/ggetters/app/data/repository/team/TeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Abstraction for loading, saving, deleting, and syncing [Team] entities.
 *
 * Implementations may back this interface with a local cache (Room), a remote
 * data source (Firestore), or a combination of both.
 */
interface TeamRepository {

    /**
     * Observe all teams.
     *
     * @return a [Flow] emitting the current list of all [Team] objects whenever
     *         the underlying data source changes.
     */
    fun observeAll(): Flow<List<Team>>

    /**
     * Fetch a single [Team] by its UUID.
     *
     * @param id the UUID of the team to retrieve
     * @return the matching [Team], or `null` if not found
     */
    suspend fun getById(id: UUID): Team?

    /**
     * Insert or update a [Team].
     *
     * If a team with the same UUID already exists, it should be replaced.
     *
     * @param team the [Team] object to save
     */
    suspend fun save(team: Team)

    /**
     * Delete a [Team] by its UUID.
     *
     * @param id the UUID of the team to delete
     */
    suspend fun delete(id: UUID)

    /**
     * Synchronize local cache with remote data.
     *
     * For combined implementations, this will pull down all teams from the
     * remote data source and upsert them into the local cache.
     */
    suspend fun sync()
}

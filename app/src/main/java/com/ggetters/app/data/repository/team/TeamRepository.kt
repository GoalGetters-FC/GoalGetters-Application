// app/src/main/java/com/ggetters/app/data/repository/team/TeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Abstraction for loading, saving, deleting and syncing Teams.
 */
interface TeamRepository {
    fun observeAll(): Flow<List<Team>>
    suspend fun getById(id: UUID): Team?
    suspend fun save(team: Team)
    suspend fun delete(id: UUID)
    suspend fun sync()  // pull remote into local
}

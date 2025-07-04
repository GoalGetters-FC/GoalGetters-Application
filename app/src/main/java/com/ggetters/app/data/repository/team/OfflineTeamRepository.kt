// app/src/main/java/com/ggetters/app/data/repository/team/OfflineTeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineTeamRepository @Inject constructor(
    private val dao: TeamDao
) : TeamRepository {

    override fun observeAll(): Flow<List<Team>> =
        dao.getAll()

    override suspend fun getById(id: UUID): Team? =
        dao.getById(id.toString()).firstOrNull()

    override suspend fun save(team: Team) =
        dao.upsert(team)

    override suspend fun delete(id: UUID) =
        dao.deleteById(id.toString())

    override suspend fun sync() {
        // no-op for local
    }
}

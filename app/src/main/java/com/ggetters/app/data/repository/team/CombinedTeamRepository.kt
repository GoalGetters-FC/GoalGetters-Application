package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.team.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinedTeamRepository @Inject constructor(
    private val offline: TeamRepository,
    private val online : TeamRepository
) : TeamRepository {

    override fun observeAll(): Flow<List<Team>> =
        offline.observeAll()

    override suspend fun getById(id: UUID): Team? =
        offline.getById(id)

    override suspend fun save(team: Team) {
        offline.save(team)
        try {
            online.save(team)
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to save team remotely", e)
        }
    }

    override suspend fun delete(id: UUID) {
        offline.delete(id)
        try {
            online.delete(id)
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to delete team remotely", e)
        }
    }

    override suspend fun sync() {
        try {
            val remoteList = online.observeAll().first()
            remoteList.forEach { offline.save(it) }
        } catch (e: Exception) {
            Clogger.e("CombinedTeamRepository", "Failed to sync teams", e)
        }
    }
}

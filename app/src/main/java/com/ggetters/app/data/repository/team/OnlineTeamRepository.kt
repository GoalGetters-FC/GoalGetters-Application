package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.data.remote.firestore.TeamFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class OnlineTeamRepository @Inject constructor(
    private val fs: TeamFirestore
) : TeamRepository {

    override fun all(): Flow<List<Team>> = fs.observeAll()

    override suspend fun getById(id: String): Team? = fs.getById(id)

    override suspend fun upsert(entity: Team) = fs.save(entity)

    override suspend fun delete(entity: Team) = fs.delete(entity.id)

    override suspend fun deleteAll() { /* no-op */ }

    override suspend fun sync() { /* no-op */ }

    override suspend fun setActiveTeam(team: Team) { /* no-op */ }

    override fun getActiveTeam(): Flow<Team?> = flowOf(null)

    override suspend fun getByCode(code: String): Team? = fs.getByCode(code)

    override suspend fun joinTeam(teamId: String) {
        fs.joinTeam(teamId) // Default role = "PLAYER"
    }

    suspend fun joinTeam(teamId: String, role: String) {
        fs.joinTeam(teamId, role)
    }

    override suspend fun joinOrCreateTeam(code: String): Team {
        val existingTeam = getByCode(code)
        return existingTeam ?: Team(
            code = code,
            name = "Remote New Team",
            composition = TeamComposition.UNISEX_MALE,
            denomination = TeamDenomination.OPEN,
        ).also { fs.save(it) }
    }

    override suspend fun createTeam(team: Team): Team {
        fs.save(team)
        fs.joinTeam(team.id)
        return team
    }
}

package com.ggetters.app.data.repository.team

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OfflineTeamRepository @Inject constructor(
    private val dao: TeamDao
) : TeamRepository {

    override fun all(): Flow<List<Team>> =
        dao.getAll()                                     // Flow<List<Team>>

    override suspend fun getById(id: String): Team? =
        dao.getById(id).first()                          // take first emission

    override suspend fun upsert(entity: Team) =
        dao.upsert(entity)

    override suspend fun delete(entity: Team) =
        dao.deleteById(entity.id)

    override suspend fun deleteAll(){
        dao.deleteAll()                                  // delete all teams
    }

    override suspend fun sync() {
        // no-op
    }

    override suspend fun setActiveTeam(team: Team) {
        dao.clearActive()
        dao.upsert(team.copy(isActive = true))
    }

    override fun getActiveTeam(): Flow<Team?> =
        dao.getActiveTeam()

    override suspend fun getByCode(code: String): Team? =
        dao.getByCode(code)

    override suspend fun joinOrCreateTeam(code: String): Team {
        var team = dao.getByCode(code)

        if (team == null) {
            // Create a new team with just a code, default values (placeholder)
            team = Team(
                code = code,
                name = "New Team", // Replace this with a proper form input in real use
                composition = TeamComposition.UNISEX_MALE,
                denomination = TeamDenomination.OPEN
            )
        }

        // Save (or update) locally and mark active
        dao.upsert(team)
        dao.clearActive()
        dao.setActiveTeam(team.id)

        return team
    }

    override suspend fun joinTeam(teamId: String) {
        val team = dao.getById(teamId).first() ?: return
        dao.clearActive()
        dao.setActiveTeam(teamId)
    }

    override suspend fun createTeam(team: Team): Team {
        // Save locally and set active
        upsert(team)
        setActiveTeam(team)
        return team
    }


}

// app/src/main/java/com/ggetters/app/data/repository/team/OnlineTeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.remote.firestore.TeamFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineTeamRepository @Inject constructor(
    private val fs: TeamFirestore
) : TeamRepository {

    override fun observeAll(): Flow<List<Team>> =
        fs.observeAllTeams()

    override suspend fun getById(id: UUID): Team? =
        fs.fetchTeam(id.toString())

    override suspend fun save(team: Team) =
        fs.saveTeam(team)

    override suspend fun delete(id: UUID) =
        fs.deleteTeam(id.toString())

    override suspend fun sync() {
        // optional: pull fs.observeAllTeams().first() into local
    }
}

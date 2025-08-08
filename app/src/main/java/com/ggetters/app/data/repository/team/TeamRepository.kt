// app/src/main/java/com/ggetters/app/data/repository/team/TeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow

/** A TeamRepository is a CRUD contract (with String IDs) plus a sync hook. */
interface TeamRepository : CrudRepository<Team> {
    suspend fun sync()
    suspend fun setActiveTeam(team: Team)
    fun getActiveTeam(): Flow<Team?>
    suspend fun getByCode(code: String): Team?
    suspend fun joinTeam(teamId: String)
    suspend fun joinOrCreateTeam(code: String): Team

    suspend fun createTeam(team: Team): Team

    fun getTeamsForCurrentUser(): Flow<List<Team>>

}

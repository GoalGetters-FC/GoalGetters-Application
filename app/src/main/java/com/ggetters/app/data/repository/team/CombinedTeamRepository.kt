package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedTeamRepository @Inject constructor(
    private val offline: OfflineTeamRepository,
    private val online: OnlineTeamRepository
) : TeamRepository {

    override fun all() = offline.all()

    override suspend fun getById(id: String): Team? =
        offline.getById(id) ?: online.getById(id)

    override suspend fun upsert(entity: Team) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            // TODO: Handle online upsert failure, e.g., log it or retry later
            // For now, we just log the error
            Clogger.e("DevClass", "failed to upsert team online: ${e.message}")
        }
    }

    override suspend fun delete(entity: Team) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "failed to delete team online: ${e.message}")
        }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
        try {
            online.deleteAll()
        } catch (e: Exception) {
            Clogger.e("DevClass", "failed to delete all teams online: ${e.message}")
        }
    }

    override suspend fun sync() {
        // Pull all from remote...
        val remoteList = online.all().first()
        // ...and upsert each one locally
        remoteList.forEach { team ->
            offline.upsert(team)
        }
    }

    override suspend fun setActiveTeam(team: Team) {
        offline.setActiveTeam(team)
    }

    override fun getActiveTeam(): Flow<Team?> =
        offline.getActiveTeam()

    override suspend fun getByCode(code: String): Team? =
        offline.getByCode(code)
        ?: online.getByCode(code) // needs to work online

    override suspend fun joinOrCreateTeam(code: String): Team {
        var team = offline.getByCode(code)

        if (team == null) {
            team = try {
                online.getByCode(code)?.also {
                    offline.upsert(it)
                }
            } catch (e: Exception) {
                null
            }
        }

        if (team == null) {
            team = Team(
                code = code,
                name = "New Team", // ← replace this with real UI flow later
                alias = null,
                description = null,
                composition = TeamComposition.UNISEX_MALE,
                denomination = TeamDenomination.OPEN,
            )
            offline.upsert(team)
            try {
                online.upsert(team)
            } catch (e: Exception) {
                // Firestore save failed — log or retry later
            }
        }

        try {
            online.joinTeam(team.id)
        } catch (e: Exception) {
            // Log join failure but proceed offline
        }

        offline.setActiveTeam(team)

        return team
    }

    override suspend fun joinTeam(teamId: String) {
        val team = getById(teamId) ?: throw IllegalArgumentException("Team not found: $teamId")

        try {
            online.joinTeam(teamId)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to join team online: ${e.message}")
        }

        offline.setActiveTeam(team)
    }


    override suspend fun createTeam(team: Team): Team {
        // 1. Save locally
        offline.upsert(team)
        Clogger.i("DevClass", "Team saved locally: ${team.id}, Code: ${team.code}")

        // 2. Save remotely
        try {
            online.upsert(team)
            Clogger.i("DevClass", "Team saved online: ${team.id}, Code: ${team.code}")

        } catch (e: Exception) {
            Clogger.i("DevClass", "Failed to create team online: ${e.message}")
        }

        // 3. Join user to team as COACH
        try {
            online.joinTeam(team.id, role = "COACH")
        } catch (e: Exception) {
            Clogger.i("DevClass", "Failed to join team online: ${e.message}")
        }

        // 4. Mark active locally
        offline.setActiveTeam(team)

        return team
    }
}



// sync
/**
 * Performs a bidirectional sync between local RoomDB and remote Firestore.
 *
 * 🔽 Pull:
 * - Fetches all remote teams from Firestore.
 * - Upserts them into the local RoomDB.
 *
 * 🔼 Push:
 * - Finds locally modified (dirty) teams.
 * - Pushes them to Firestore.
 * - Marks them clean once synced.
 *
 * ⚠️ Conflict Strategy:
 * - To be implemented. Options: Last-write-wins, remote wins, merge logic, etc.
 *
 * TODO:
 * - Implement `getDirtyTeams()` and `markClean()` in DAO.
 * - Ensure all local writes set `dirty = true`.
 */


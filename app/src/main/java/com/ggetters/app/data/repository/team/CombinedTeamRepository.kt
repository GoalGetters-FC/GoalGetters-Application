package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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

    // CombinedTeamRepository.kt
    override suspend fun sync() = withContext(Dispatchers.IO) {
        // snapshot local teams before we mutate anything
        val localSnapshot = offline.getAllLocal().associateBy { it.id }

        // 1) PUSH local changes — and ensure membership for new teams
        val pushedIds = mutableSetOf<String>()
        offline.getDirtyTeams().first().forEach { team ->
            try {
                online.upsert(team)
                // ensure the current user is a member so the team appears in "pull"
                try {
                    online.joinTeam(team.id)  // role = default "PLAYER" in OnlineTeamRepository
                } catch (_: Exception) {
                    // membership may already exist (ignore) or fail transiently
                }
                offline.markClean(team.id)
                pushedIds += team.id
            } catch (e: Exception) {
                Clogger.e("Sync", "Failed to push team ${team.id}", e)
            }
        }

        // 2) PULL remote teams for this user (one-shot)
        val remote = online.fetchTeamsForCurrentUser()
        val remoteIds = remote.map { it.id }.toSet()
        Clogger.i("Sync", "Pulled ${remote.size} remote teams")

        // 3) CLEAN UP stale locals (skip just-pushed ids to avoid race with Firestore read-your-writes)
        val toDelete = offline.getAllLocal()
            .filter { it.id !in remoteIds && !it.isStained() && it.id !in pushedIds }
        toDelete.forEach { offline.deleteByIdLocal(it.id) }
        Clogger.i("Sync", "Dropped ${toDelete.size} stale locals")

        // 4) MERGE down, preserving local-only flags (e.g., isActive)
        val merged = remote.map { remoteTeam ->
            val localOld = localSnapshot[remoteTeam.id]
            if (localOld != null) {
                remoteTeam.copy(
                    isActive = localOld.isActive  // preserve local active flag
                    // stainedAt should remain null for remote items
                )
            } else remoteTeam
        }

        // batch upsert in a transaction (Room DAO should handle this efficiently)
        offline.upsertAllLocal(merged)

        // 5) ensure we still have an active team — pick a sensible default if not
        val activeNow = offline.getActiveTeam().first()
        if (activeNow == null && merged.isNotEmpty()) {
            // if previously active still exists, keep it; otherwise first remote
            val previouslyActive = localSnapshot.values.firstOrNull { it.isActive && it.id in remoteIds }
            offline.setActiveTeam(previouslyActive ?: merged.first())
        }

        Clogger.i("Sync", "Merged ${merged.size} remote into local")
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

    override fun getTeamsForCurrentUser(): Flow<List<Team>> {
        // Use online if available, otherwise offline
        // This example just returns offline for now (replace with merge logic if needed)
        return offline.getTeamsForCurrentUser()
    }

}






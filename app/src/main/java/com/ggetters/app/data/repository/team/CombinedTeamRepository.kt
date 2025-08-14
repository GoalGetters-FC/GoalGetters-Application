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
        offline.getById(id) ?: online.getById(id) // ok to keep this fallback

    // 🔧 local-only upsert (no online call here)
    override suspend fun upsert(entity: Team) {
        offline.upsert(entity) // stains
    }

    // combined repo
    override suspend fun delete(entity: Team) {
        offline.delete(entity)
        // deletion propagation handled by sync() if you add tombstones later

        // Best-effort remote (leave team or delete, depending on role)
        runCatching { online.leaveOrDelete(entity.id) }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
    }

    // ✅ periodic sync handles push/pull/membership/merge, preserving isActive
    override suspend fun sync() = withContext(Dispatchers.IO) {
        val localSnapshot = offline.getAllLocal().associateBy { it.id }

        val pushedIds = mutableSetOf<String>()
        offline.getDirtyTeams().first().forEach { team ->
            try {
                online.upsert(team)
                try { online.joinTeam(team.id) } catch (_: Exception) {}
                offline.markClean(team.id)
                pushedIds += team.id
            } catch (e: Exception) {
                Clogger.e("Sync", "Failed to push team ${team.id}", e)
            }
        }

        val remote = online.fetchTeamsForCurrentUser()
        val remoteIds = remote.map { it.id }.toSet()
        Clogger.i("Sync", "Pulled ${remote.size} remote teams")

        val toDelete = offline.getAllLocal()
            .filter { it.id !in remoteIds && !it.isStained() && it.id !in pushedIds }
        toDelete.forEach { offline.deleteByIdLocal(it.id) }
        Clogger.i("Sync", "Dropped ${toDelete.size} stale locals")

        val merged = remote.map { r ->
            localSnapshot[r.id]?.let { r.copy(isActive = it.isActive) } ?: r
        }
        offline.upsertAllLocal(merged)

        val activeNow = offline.getActiveTeam().first()
        if (activeNow == null && merged.isNotEmpty()) {
            val prevActive = localSnapshot.values.firstOrNull { it.isActive && it.id in remoteIds }
            offline.setActiveTeam(prevActive ?: merged.first())
        }
        Clogger.i("Sync", "Merged ${merged.size} remote into local")
    }

    override suspend fun setActiveTeam(team: Team) = offline.setActiveTeam(team)
    override fun getActiveTeam() = offline.getActiveTeam()

    override suspend fun getByCode(code: String): Team? =
        offline.getByCode(code) ?: online.getByCode(code)

    // 🔧 create local-only; joining happens on sync()
    override suspend fun createTeam(team: Team): Team {
        offline.upsert(team)
        offline.setActiveTeam(team)
        return team
    }

    override suspend fun joinTeam(teamId: String) {
        val team = getById(teamId) ?: throw IllegalArgumentException("Team not found: $teamId")
        try { online.joinTeam(teamId) } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to join team online: ${e.message}")
        }
        offline.setActiveTeam(team)
    }

    // ok to keep; UI may still rely on it
    override suspend fun joinOrCreateTeam(code: String): Team {
        var team = offline.getByCode(code)
            ?: runCatching { online.getByCode(code) }.getOrNull()?.also { offline.upsert(it) }
            ?: Team(code = code, name = "New Team")

        offline.upsert(team)
        try { online.joinTeam(team.id) } catch (_: Exception) {}
        offline.setActiveTeam(team)
        return team
    }

    override fun getTeamsForCurrentUser() = offline.getTeamsForCurrentUser()
}






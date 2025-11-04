package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.google.firebase.perf.FirebasePerformance
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

    override suspend fun getById(id: String): Team? {
        val trace = FirebasePerformance.getInstance().newTrace("teamrepo_getById")
        trace.start()
        try {
            val team = offline.getById(id) ?: online.getById(id)
            if (team != null) trace.putMetric("team_found", 1) else trace.putMetric("team_found", 0)
            return team
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsert(entity: Team) {
        offline.upsert(entity) // local-only, let sync push it
    }

    override suspend fun delete(entity: Team) {
        offline.delete(entity)
        runCatching { online.leaveOrDelete(entity.id) }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
    }

    override suspend fun sync() = withContext(Dispatchers.IO) {
        val trace = FirebasePerformance.getInstance().newTrace("teamrepo_sync")
        trace.start()
        try {
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
            trace.putMetric("teams_pushed", pushedIds.size.toLong())

            val remote = online.fetchTeamsForCurrentUser()
            val remoteIds = remote.map { it.id }.toSet()
            Clogger.i("Sync", "Pulled ${remote.size} remote teams")
            trace.putMetric("teams_pulled", remote.size.toLong())

            // Be conservative: skip local deletion to avoid accidental disappearance
            trace.putMetric("teams_deleted_local", 0)

            val merged = remote.map { r ->
                localSnapshot[r.id]?.let { r.copy(isActive = it.isActive) } ?: r
            }
            offline.upsertAllLocal(merged)
            trace.putMetric("teams_merged", merged.size.toLong())

            val activeNow = offline.getActiveTeam().first()
            if (activeNow == null && merged.isNotEmpty()) {
                val prevActive = localSnapshot.values.firstOrNull { it.isActive && it.id in remoteIds }
                offline.setActiveTeam(prevActive ?: merged.first())
            }
        } finally {
            trace.stop()
        }
    }

    override suspend fun setActiveTeam(team: Team) = offline.setActiveTeam(team)

    override fun getActiveTeam() = offline.getActiveTeam()

    override fun getByIdFlow(id: String): Flow<Team?> = offline.getByIdFlow(id)

    override suspend fun getByCode(code: String): Team? {
        val trace = FirebasePerformance.getInstance().newTrace("teamrepo_getByCode")
        trace.start()
        try {
            return offline.getByCode(code) ?: online.getByCode(code)
        } finally {
            trace.stop()
        }
    }

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

    override suspend fun joinOrCreateTeam(code: String): Team {
        val trace = FirebasePerformance.getInstance().newTrace("teamrepo_joinOrCreateTeam")
        trace.start()
        try {
            var team = offline.getByCode(code)
                ?: runCatching { online.getByCode(code) }.getOrNull()?.also { offline.upsert(it) }
                ?: Team(code = code, name = "New Team")

            offline.upsert(team)
            try { online.joinTeam(team.id) } catch (_: Exception) {}
            offline.setActiveTeam(team)
            return team
        } finally {
            trace.stop()
        }
    }

    override fun getTeamsForCurrentUser() = offline.getTeamsForCurrentUser()

    override suspend fun updateTeamCode(teamId: String, code: String) {
        offline.updateTeamCode(teamId, code)
    }
}

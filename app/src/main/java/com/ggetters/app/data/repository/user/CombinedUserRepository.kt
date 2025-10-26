package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class CombinedUserRepository @Inject constructor(
    private val offline: OfflineUserRepository,
    private val online: OnlineUserRepository,
    private val teamRepo: com.ggetters.app.data.repository.team.TeamRepository
) : UserRepository {

    // Stream members for the active team
    fun allForActiveTeam(): Flow<List<User>> =
        teamRepo.getActiveTeam().flatMapLatest { team ->
            team?.let { offline.allForTeam(it.id) } ?: flowOf(emptyList())
        }

    override fun all(): Flow<List<User>> = allForActiveTeam()

    override suspend fun getById(id: String): User? {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_getById")
        trace.start()
        try {
            val teamId = teamRepo.getActiveTeam().first()?.id ?: return null
            val user = offline.getById(teamId, id) ?: online.getById(teamId, id)
            if (user != null) trace.putMetric("user_found", 1) else trace.putMetric("user_found", 0)
            return user
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsert(entity: User) {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_upsert")
        trace.start()
        try {
            offline.upsert(entity)
            trace.putMetric("user_upserted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun delete(entity: User) {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_delete")
        trace.start()
        try {
            offline.delete(entity)
            trace.putMetric("user_deleted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun deleteAll() {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_deleteAll")
        trace.start()
        try {
            teamRepo.getActiveTeam().first()?.let { offline.deleteAll() }
            trace.putMetric("users_deleted_all", 1)
        } finally {
            trace.stop()
        }
    }

    // 🔁 Two-way sync for active team (push dirty, pull remote)
    override suspend fun sync() {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_sync")
        trace.start()
        try {
            val team = teamRepo.getActiveTeam().first() ?: return
            val teamId = team.id

            // push dirty
            var pushed = 0
            offline.getDirtyUsers(teamId).forEach { u ->
                runCatching { online.upsert(u) }
                    .onSuccess { offline.markClean(u.id, teamId); pushed++ }
            }
            trace.putMetric("users_pushed", pushed.toLong())

            // pull + merge
            val remote = online.fetchAllForTeam(teamId)
            trace.putMetric("users_pulled", remote.size.toLong())

            offline.upsertAllLocal(remote.map { r ->
                val local = offline.getById(teamId, r.id)
                r.copy(updatedAt = maxOf(r.updatedAt, local?.updatedAt ?: r.updatedAt))
            })
            trace.putMetric("users_merged", remote.size.toLong())
        } finally {
            trace.stop()
        }
    }

    // convenience join helpers
    suspend fun joinActiveTeamAsPlayer(authId: String) {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_joinActiveTeamAsPlayer")
        trace.start()
        try {
            val team = teamRepo.getActiveTeam().first() ?: return
            online.joinTeam(team.id, authId, UserRole.FULL_TIME_PLAYER)
            trace.putMetric("player_joined", 1)
        } finally {
            trace.stop()
        }
    }

    suspend fun becomeCoachOf(teamId: String, authId: String) {
        val trace = FirebasePerformance.getInstance().newTrace("userrepo_becomeCoachOf")
        trace.start()
        try {
            online.joinTeam(teamId, authId, UserRole.COACH)
            trace.putMetric("coach_assigned", 1)
        } finally {
            trace.stop()
        }
    }

    // Get user by Firebase Auth ID (global across all teams)
    override suspend fun getLocalByAuthId(authId: String): User? = offline.getLocalByAuthId(authId)
    override suspend fun insertLocal(user: User) = offline.insertLocal(user)
    override suspend fun insertRemote(user: User) = online.insertRemote(user)

    override fun hydrateForTeam(id: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val trace = FirebasePerformance.getInstance().newTrace("userrepo_hydrateForTeam")
            trace.start()
            try {
                Clogger.d("UserRepo", "Hydrating users for team $id …")
                sync()
                trace.putMetric("hydrated", 1)
            } catch (e: Exception) {
                Clogger.e("UserRepo", "Hydrate failed for team $id", e)
                trace.putMetric("hydrate_failed", 1)
            } finally {
                trace.stop()
            }
        }
    }
}

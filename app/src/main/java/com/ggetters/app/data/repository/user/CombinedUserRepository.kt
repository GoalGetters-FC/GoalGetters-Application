package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

// app/src/main/java/com/ggetters/app/data/repository/user/CombinedUserRepository.kt
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
        val teamId = teamRepo.getActiveTeam().first()?.id ?: return null
        return offline.getById(teamId, id) ?: online.getById(teamId, id)
    }

    override suspend fun upsert(entity: User) = offline.upsert(entity)

    override suspend fun delete(entity: User) = offline.delete(entity)

    override suspend fun deleteAll() {
        teamRepo.getActiveTeam().first()?.let { offline.deleteAll() } // prob broken
    }

    // 🔁 Two-way sync for active team (push dirty, pull remote)
    override suspend fun sync() {
        val team = teamRepo.getActiveTeam().first() ?: return
        val teamId = team.id

        // push dirty
        offline.getDirtyUsers(teamId).forEach { u ->
            runCatching { online.upsert(u) }.onSuccess { offline.markClean(u.id, teamId) }
        }

        // pull + merge
        val remote = online.fetchAllForTeam(teamId)
        offline.upsertAllLocal(remote.map { r ->
            val local = offline.getById(teamId, r.id)
            r.copy(updatedAt = maxOf(r.updatedAt, local?.updatedAt ?: r.updatedAt))
        })
    }

    // convenience join helpers (respect your rules)
    suspend fun joinActiveTeamAsPlayer(authId: String) {
        val team = teamRepo.getActiveTeam().first() ?: return
        online.joinTeam(team.id, authId, UserRole.FULL_TIME_PLAYER)
    }

    suspend fun becomeCoachOf(teamId: String, authId: String) {
        online.joinTeam(teamId, authId, UserRole.COACH)
    }

    // Unused CrudRepository methods (kept for interface)
    override suspend fun getLocalByAuthId(authId: String): User? = null
    override suspend fun insertLocal(user: User) = offline.insertLocal(user)
    override suspend fun insertRemote(user: User) = online.insertRemote(user)

    override fun hydrateForTeam(id: String) {
        // Run sync in the background so it won't block UI
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                Clogger.d("UserRepo", "Hydrating users for team $id …")
                sync()
            } catch (e: Exception) {
                Clogger.e("UserRepo", "Hydrate failed for team $id", e)
            }
        }
    }

}

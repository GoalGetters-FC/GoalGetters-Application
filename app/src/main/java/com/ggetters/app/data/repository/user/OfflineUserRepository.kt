package com.ggetters.app.data.repository.user

import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.User
import com.ggetters.app.core.utils.Clogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

// app/src/main/java/com/ggetters/app/data/repository/user/OfflineUserRepository.kt
class OfflineUserRepository @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    fun allForTeam(teamId: String): Flow<List<User>> = dao.getByTeamId(teamId)

    override fun all(): Flow<List<User>> = flowOf(emptyList())

    override suspend fun getById(id: String): User? = null

    suspend fun getById(teamId: String, id: String): User? = dao.getByIdInTeam(id, teamId)

    override suspend fun upsert(entity: User) {
        // Avoid crashing if the entity is already marked dirty
        try {
            if (entity.isCleaned()) {
                entity.stain()
            } else {
                Clogger.w("UserRepo", "Upsert called with already-dirty entity; proceeding without re-stain")
            }
        } catch (e: IllegalStateException) {
            // Defensive: proceed without re-staining
            Clogger.w("UserRepo", "stain() threw on upsert; proceeding: ${e.message}")
        }
        dao.upsert(entity)
    }

    override suspend fun delete(entity: User) = entity.teamId?.let { teamId ->
        dao.deleteByIdInTeam(entity.id, teamId)
    } ?: run {
        // If no teamId, just delete by ID
        dao.deleteById(entity.id)
    }

    override suspend fun deleteAll() { /* optional global wipe */ }

    override suspend fun sync() { /* no-op */ }

    override fun hydrateForTeam(id: String) {}

    suspend fun upsertAllLocal(users: List<User>) = dao.upsertAll(users)
    suspend fun getDirtyUsers(teamId: String) = dao.getDirtyUsers(teamId)
    suspend fun markClean(id: String, teamId: String) = dao.markClean(id, teamId)

    override suspend fun getLocalByAuthId(authId: String): User? = null
    override suspend fun insertLocal(user: User) = dao.upsert(user)
    override suspend fun insertRemote(user: User) { /* no-op */ }
}

package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.remote.firestore.UserFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID
import javax.inject.Inject

// app/src/main/java/com/ggetters/app/data/repository/user/OnlineUserRepository.kt
class OnlineUserRepository @Inject constructor(
    private val fs: UserFirestore
) : UserRepository {

    // Caller supplies teamId everywhere in Combined repo
    fun allForTeam(teamId: String): Flow<List<User>> = fs.observeForTeam(teamId)
    suspend fun fetchAllForTeam(teamId: String): List<User> = fs.fetchAll(teamId)

    override fun all(): Flow<List<User>> = emptyFlow() // avoid accidental global
    override suspend fun getById(id: String): User? = null

    suspend fun getById(teamId: String, id: String): User? = fs.getById(teamId, id)

    override suspend fun upsert(entity: User) { 
        entity.teamId?.let { teamId ->
            fs.upsert(teamId, entity) 
        } ?: run {
            // If no teamId, skip online upsert (user not assigned to team yet)
            Clogger.w("OnlineUserRepo", "Skipping upsert for user ${entity.id} - no teamId assigned")
        }
    }
    override suspend fun delete(entity: User) { 
        entity.teamId?.let { teamId ->
            fs.delete(teamId, entity.id) 
        } ?: run {
            // If no teamId, skip online delete
            Clogger.w("OnlineUserRepo", "Skipping delete for user ${entity.id} - no teamId assigned")
        }
    }

    override suspend fun deleteAll() { /* prob broken */ }
    override suspend fun sync() { /* no-op */ }
    override fun hydrateForTeam(id: String) { /* no-op here */ }

    // convenience for role-based join from Team create/join flows
    suspend fun joinTeam(teamId: String, authId: String, role: UserRole, seed: User? = null) {
        val base = seed ?: User(
            id = authId, authId = authId, teamId = teamId,
            role = role
        ).apply { notifyJoinedTeam() }
        fs.upsert(teamId, base)
    }

    override suspend fun getLocalByAuthId(authId: String): User? = null
    override suspend fun insertLocal(user: User) { /* no-op */ }
    override suspend fun insertRemote(user: User) { 
        user.teamId?.let { teamId ->
            fs.upsert(teamId, user) 
        } ?: run {
            Clogger.w("OnlineUserRepo", "Skipping remote insert for user ${user.id} - no teamId assigned")
        }
    }
}

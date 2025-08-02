package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedUserRepository @Inject constructor(
    private val offline: OfflineUserRepository,
    private val online: OnlineUserRepository
) : UserRepository {

    override fun all() = offline.all()

    // ← now takes a String, not UUID
    override suspend fun getById(id: String): User? {
        // prefer local, fall back to remote
        return offline.getById(id) ?: online.getById(id)
    }

    override suspend fun upsert(entity: User) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to upsert user online: ${e.message}")
        }
    }

    override suspend fun delete(entity: User) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete user online: ${e.message}")
        }
    }

    override suspend fun getLocalByAuthId(authId: String): User? =
        offline.getLocalByAuthId(authId)

    override suspend fun insertLocal(user: User) =
        offline.insertLocal(user)

    override suspend fun insertRemote(user: User) =
        online.insertRemote(user)

    override suspend fun sync() {
        // pull from remote and write into local
        val remoteList = online.all().first()
        remoteList.forEach { offline.upsert(it) }
    }
}

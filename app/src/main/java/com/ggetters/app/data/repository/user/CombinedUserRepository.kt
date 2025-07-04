package com.ggetters.app.data.repository.user

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinedUserRepository @Inject constructor(
    private val offline: UserRepository,
    private val online: UserRepository
) : UserRepository {

    override fun observeAll(): Flow<List<User>> =
        offline.observeAll()

    override suspend fun getById(id: UUID): User? =
        offline.getById(id)

    override suspend fun save(user: User) {
        offline.save(user)
        try {
            online.save(user)
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to save user remotely", e)
        }
    }

    override suspend fun delete(id: UUID) {
        offline.delete(id)
        try {
            online.delete(id)
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to delete user remotely", e)
        }
    }

    override suspend fun sync() {
        try {
            val remoteList = online.observeAll().first()
            remoteList.forEach { offline.save(it) }
        } catch (e: Exception) {
            Clogger.e("CombinedUserRepository", "Failed to sync users", e)
        }
    }
}

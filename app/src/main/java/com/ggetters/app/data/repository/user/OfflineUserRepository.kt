package com.ggetters.app.data.repository.user

import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room‐backed implementation of UserRepository.
 */
@Singleton
class OfflineUserRepository @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    override fun observeAll(): Flow<List<User>> =
        dao.getAll()

    override suspend fun getById(id: UUID): User? =
        dao.getByIdOnce(id)

    override suspend fun save(user: User): Unit =
        dao.upsert(user)

    override suspend fun delete(id: UUID): Unit =
        dao.deleteById(id)

    override suspend fun sync(): Unit {
        // no-op for local
    }
}

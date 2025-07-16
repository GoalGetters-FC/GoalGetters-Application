package com.ggetters.app.data.repository.user

import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class OfflineUserRepository @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    override fun all(): Flow<List<User>> =
        dao.getAll()

//    override suspend fun getById(id: UUID): User? =
//        dao.getByIdOnce(id)         // pass UUID directly

    override suspend fun upsert(entity: User) =
        dao.upsert(entity)

//    override suspend fun delete(entity: User) {
//        dao.deleteById(entity.id)   // pass UUID directly
//    }

    override suspend fun getById(id: String): User? =
        dao.getByIdOnce(UUID.fromString(id)) // if your DAO still wants UUID

    override suspend fun delete(entity: User) =
        dao.deleteById(UUID.fromString(entity.id))

    override suspend fun getLocalByAuthId(authId: String): User? =
        dao.getByAuthId(authId)

    override suspend fun insertLocal(user: User) =
        dao.upsert(user)

    override suspend fun insertRemote(user: User) {
        // no-op
    }

    override suspend fun sync() {
        // no-op
    }
}

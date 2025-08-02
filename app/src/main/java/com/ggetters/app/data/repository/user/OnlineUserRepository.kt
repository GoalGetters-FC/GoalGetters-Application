package com.ggetters.app.data.repository.user

import com.ggetters.app.data.model.User
import com.ggetters.app.data.remote.firestore.UserFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class OnlineUserRepository @Inject constructor(
    private val fs: UserFirestore
) : UserRepository {

    override fun all(): Flow<List<User>> =
        fs.observeAll()

//    override suspend fun getById(id: UUID): User? =
//        fs.getById(id.toString())     // convert UUID→String here

    override suspend fun upsert(entity: User) =
        fs.save(entity)

//    override suspend fun delete(entity: User) =
//        fs.delete(entity.id.toString())  // ensure you pass String here

    override suspend fun getById(id: String): User? =
        fs.getById(id)

    override suspend fun delete(entity: User) =
        fs.delete(entity.id)


    override suspend fun getLocalByAuthId(authId: String): User? =
        null  // offline-only

    override suspend fun insertLocal(user: User) {
        // no-op
    }

    override suspend fun insertRemote(user: User) =
        fs.save(user)

    override suspend fun sync() {
        // no-op
    }
}

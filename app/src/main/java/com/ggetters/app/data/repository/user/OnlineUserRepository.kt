package com.ggetters.app.data.repository.user

import com.ggetters.app.data.model.User
import com.ggetters.app.data.remote.firestore.UserFirestore
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore‐backed implementation of UserRepository.
 */
@Singleton
class OnlineUserRepository @Inject constructor(
    private val firestore: UserFirestore
) : UserRepository {

    override fun observeAll(): Flow<List<User>> =
        firestore.observeAll()

    override suspend fun getById(id: UUID): User? =
        firestore.getById(id)

    override suspend fun save(user: User): Unit =
        firestore.save(user)

    override suspend fun delete(id: UUID): Unit =
        firestore.delete(id)

    override suspend fun sync(): Unit {
        // optional: fetch remote and write into local
    }
}

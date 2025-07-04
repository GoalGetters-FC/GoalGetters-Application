// app/src/main/java/com/ggetters/app/data/repository/UserRepository.kt
package com.ggetters.app.data.repository.user

import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Abstraction for loading, saving, deleting and syncing Users.
 */
interface UserRepository {
    fun observeAll(): Flow<List<User>>
    suspend fun getById(id: UUID): User?
    suspend fun save(user: User)
    suspend fun delete(id: UUID)
    suspend fun sync()   // e.g. pull remote into local
}

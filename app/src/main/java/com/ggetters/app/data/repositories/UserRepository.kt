package com.ggetters.app.data.repositories

import com.ggetters.app.data.daos.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.ggetters.app.data.mappers.toEntity
import com.ggetters.app.data.models.User

/**
 * com.ggetters.app.data.repository
 *
 * Contains the “single source of truth” abstractions that coordinate
 * between local (Room) and remote (Firestore) data sources.
 *
 * Each Repository:
 *  - Exposes domain‐friendly methods (e.g. observeUser, saveUser, syncUser)
 *  - Knows how to map Entities ↔ DTOs
 *  - Decides when to read/write locally vs. remotely
 *
 * Examples:
 *  - UserRepository
 *  - TeamRepository
 */

class UserRepository(
    private val dao: UserDao,
    private val remote: UserFirestore
) {
    fun observeAll(): Flow<List<User>> =
        dao.getAll()

    suspend fun syncAll() {
        val dtos = remote.watchAllUsers().first()  // one-time snapshot
        dao.upsertAll(dtos.map { it.toEntity() })
    }

    suspend fun save(entity: User) {
        dao.upsert(entity)
        remote.saveUser(entity)
    }
}

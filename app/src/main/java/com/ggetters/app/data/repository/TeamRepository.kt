package com.ggetters.app.data.repository

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.entities.TeamEntity
import com.ggetters.app.data.remote.firestore.TeamFirestore
import com.ggetters.app.data.remote.mappers.toDto
import com.ggetters.app.data.remote.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

class TeamRepository(
    private val dao: TeamDao,
    private val remote: TeamFirestore
) {
    fun observeAll(): Flow<List<TeamEntity>> =
        dao.getAll()

    suspend fun syncAll() {
        val dtos = remote.watchAllTeams().first() // one-time snapshot
        dao.upsertAll(dtos.map { it.toEntity() })
    }

    suspend fun save(entity: TeamEntity) {
        dao.upsert(entity)
        remote.saveTeam(entity.toDto())
    }
}
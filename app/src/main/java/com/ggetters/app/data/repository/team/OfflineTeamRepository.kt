package com.ggetters.app.data.repository.team

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OfflineTeamRepository @Inject constructor(
    private val dao: TeamDao
) : TeamRepository {

    override fun all(): Flow<List<Team>> =
        dao.getAll()                                     // Flow<List<Team>>

    override suspend fun getById(id: String): Team? =
        dao.getById(id).first()                          // take first emission

    override suspend fun upsert(entity: Team) =
        dao.upsert(entity)

    override suspend fun delete(entity: Team) =
        dao.deleteById(entity.id)

    override suspend fun sync() {
        // no-op
    }
}

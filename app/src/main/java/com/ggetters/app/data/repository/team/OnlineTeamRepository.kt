package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.remote.firestore.TeamFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnlineTeamRepository @Inject constructor(
    private val fs: TeamFirestore
) : TeamRepository {

    override fun all(): Flow<List<Team>> =
        fs.observeAll()

    override suspend fun getById(id: String): Team? =   // ← use String
        fs.getById(id)

    override suspend fun upsert(entity: Team) {
        fs.save(entity)
    }

    override suspend fun delete(entity: Team) {
        fs.delete(entity.id)                        // entity.id is String
    }

    override suspend fun sync() {
        // no-op
    }
}

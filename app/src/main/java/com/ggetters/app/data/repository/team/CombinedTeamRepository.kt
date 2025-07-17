package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedTeamRepository @Inject constructor(
    private val offline: OfflineTeamRepository,
    private val online: OnlineTeamRepository
) : TeamRepository {

    override fun all() = offline.all()

    override suspend fun getById(id: String): Team? =
        offline.getById(id) ?: online.getById(id)

    override suspend fun upsert(entity: Team) {
        offline.upsert(entity)
        online.upsert(entity)
    }

    override suspend fun delete(entity: Team) {
        offline.delete(entity)
        online.delete(entity)
    }

    override suspend fun sync() {
        // Pull all from remote...
        val remoteList = online.all().first()
        // ...and upsert each one locally
        remoteList.forEach { team ->
            offline.upsert(team)
        }
    }
}

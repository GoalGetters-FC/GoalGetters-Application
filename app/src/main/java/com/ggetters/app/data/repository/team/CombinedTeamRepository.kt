package com.ggetters.app.data.repository.team

import com.ggetters.app.core.utils.Clogger
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
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            // TODO: Handle online upsert failure, e.g., log it or retry later
            // For now, we just log the error
            Clogger.e("DevClass", "failed to upsert team online: ${e.message}")
        }
    }

    override suspend fun delete(entity: Team) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "failed to delete team online: ${e.message}")
        }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
        try {
            online.deleteAll()
        } catch (e: Exception) {
            Clogger.e("DevClass", "failed to delete all teams online: ${e.message}")
        }
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

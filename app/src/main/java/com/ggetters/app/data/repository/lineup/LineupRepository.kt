package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow

interface LineupRepository : CrudRepository<Lineup> {
    fun getByEventId(eventId: String): Flow<List<Lineup>>
    fun hydrateForTeam(id: String)
    fun sync()

}

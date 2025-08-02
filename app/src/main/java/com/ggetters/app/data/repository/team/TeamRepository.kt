// app/src/main/java/com/ggetters/app/data/repository/team/TeamRepository.kt
package com.ggetters.app.data.repository.team

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.repository.CrudRepository

/** A TeamRepository is a CRUD contract (with String IDs) plus a sync hook. */
interface TeamRepository : CrudRepository<Team> {
    /** Pull all Teams from Firestore and upsert into Room */
    suspend fun sync()

    suspend fun deleteAll()

}

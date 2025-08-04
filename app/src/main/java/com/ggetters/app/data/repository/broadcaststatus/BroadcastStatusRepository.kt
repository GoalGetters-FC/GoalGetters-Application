package com.ggetters.app.data.repository.broadcaststatus

import com.ggetters.app.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for BroadcastStatus, keyed by (broadcastId, recipientId).
 */

interface BroadcastStatusRepository {
    /** Stream every status in the table */
    fun all(): Flow<List<BroadcastStatus>>

    /** Stream all statuses for a given broadcast */
    fun allForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>>

    /** Fetch a single status by composite key */
    suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus?

    /** Upsert one status */
    suspend fun upsert(entity: BroadcastStatus)

    /** Delete one status */
    suspend fun delete(entity: BroadcastStatus)

    /** Delete all statuses */
    suspend fun deleteAll()
}

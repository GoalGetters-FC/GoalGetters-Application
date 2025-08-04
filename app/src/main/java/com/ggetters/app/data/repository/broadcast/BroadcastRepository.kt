// ✅ BroadcastRepository.kt
package com.ggetters.app.data.repository.broadcast

import com.ggetters.app.data.model.Broadcast
import com.ggetters.app.data.repository.CrudRepository

interface BroadcastRepository : CrudRepository<Broadcast>
{
    /**
     * Delete all broadcasts.
     */
    suspend fun deleteAll()
}


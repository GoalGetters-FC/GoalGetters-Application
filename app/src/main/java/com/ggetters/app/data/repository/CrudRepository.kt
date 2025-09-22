package com.ggetters.app.data.repository

import kotlinx.coroutines.flow.Flow

interface CrudRepository<E> {
    fun all(): Flow<List<E>>
    suspend fun getById(id: String): E?
    suspend fun upsert(entity: E)
    suspend fun delete(entity: E)
    suspend fun deleteAll()
}

package com.ggetters.app.data.repository.user

import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.CrudRepository

interface UserRepository : CrudRepository<User> {
    suspend fun getLocalByAuthId(authId: String): User?
    suspend fun insertLocal(user: User)
    suspend fun insertRemote(user: User)
    suspend fun sync()
}

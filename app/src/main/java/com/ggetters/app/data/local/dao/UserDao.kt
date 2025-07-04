// app/src/main/java/com/ggetters/app/data/local/dao/UserDao.kt
package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getByIdOnce(id: UUID): User?

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: UUID)
}

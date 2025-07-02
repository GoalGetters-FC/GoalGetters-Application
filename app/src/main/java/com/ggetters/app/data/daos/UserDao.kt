package com.ggetters.app.data.daos

import androidx.room.*
import com.ggetters.app.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE teamId = :teamId")
    fun getByTeam(teamId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)
}

package com.ggetters.app.data.daos

import androidx.room.*
import com.ggetters.app.data.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<User>)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getById(id: String): Flow<User?>

    @Query("SELECT * FROM user WHERE team_id = :teamId")
    fun getByTeam(teamId: String): Flow<List<User>>

    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: String)
}

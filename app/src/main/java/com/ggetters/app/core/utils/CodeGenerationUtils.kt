package com.ggetters.app.core.utils

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.model.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for generating collision-safe codes for entities.
 * 
 * This class provides methods to generate unique codes with collision detection
 * and retry logic to ensure database integrity.
 */
@Singleton
class CodeGenerationUtils @Inject constructor(
    private val teamDao: TeamDao
) {
    
    companion object {
        private const val TAG = "CodeGenerationUtils"
        private const val MAX_RETRY_ATTEMPTS = 10
        private const val CODE_LENGTH = 6
    }
    
    /**
     * Generates a collision-safe code for a team.
     * 
     * This method will attempt to generate a unique code by checking against
     * existing codes in the database. If a collision occurs, it will retry
     * with a new random code up to MAX_RETRY_ATTEMPTS times.
     * 
     * @return A unique code that doesn't exist in the database
     * @throws IllegalStateException if unable to generate a unique code after MAX_RETRY_ATTEMPTS
     */
    suspend fun generateCollisionSafeTeamCode(): String = withContext(Dispatchers.IO) {
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            val code = generateAlphanumericCode(CODE_LENGTH)
            val existingTeam = teamDao.getByCode(code)
            
            if (existingTeam == null) {
                Clogger.d(TAG, "Generated unique team code: $code (attempt ${attempt + 1})")
                return@withContext code
            }
            
            Clogger.w(TAG, "Code collision detected for: $code (attempt ${attempt + 1})")
        }
        
        throw IllegalStateException(
            "Unable to generate unique team code after $MAX_RETRY_ATTEMPTS attempts"
        )
    }
    
    /**
     * Generates a random alphanumeric code using SecureRandom for cryptographic security.
     * 
     * @param length The length of the code to generate
     * @return A random alphanumeric string
     */
    private fun generateAlphanumericCode(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}

// app/src/main/java/com/ggetters/app/core/utils/DevClass.kt
package com.ggetters.app.core.utils

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class DevClass @Inject constructor(
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository
) {
    private var isInitialized = false

//    fun init() {
//        if (isInitialized) return
//        isInitialized = true
//
//        CoroutineScope(Dispatchers.IO).launch {
//            // 1) Generate a single random UUID string for dev-team:
//            val teamId = UUID.randomUUID().toString()
//
//            // 2) Upsert the dummy team with that ID
//            val existing = teamRepo.getById(UUID.fromString(teamId))
//            if (existing == null) {
//                val now = Instant.now()
//                val dummyTeam = Team(
//                    id        = teamId,
//                    createdAt = now,
//                    updatedAt = now,
//                    code      = "DEV002",
//                    name      = "Dev Team"
//                )
//                teamRepo.save(dummyTeam)
//                Clogger.i("DevClass", "Seeded dummy team: $teamId")
//            }
//
//            // 3) Now seed the user using the same teamId
//            val user = User(
//                authId      = "test-${UUID.randomUUID()}",
//                teamId      = teamId,
//                role        = 2,
//                name        = "Test2",
//                surname     = "User2",
//                alias       = "tester2",
//                dateOfBirth = Date.from(Instant.now().minusSeconds(60L*60*24*365*20))
//            )
//
//            try {
//                userRepo.save(user)
//                Clogger.i("DevClass", "Seeded test user locally and remotely via save()")
//            } catch (e: Exception) {
//                Clogger.e("DevClass", "Failed to insert test user locally", e)
//            }
//        }
//    }
}

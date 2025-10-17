package com.ggetters.app.ui.management.viewmodels

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Handles team-leaving and membership logic for TeamViewerViewModel.
 * Determines how to handle leave operations:
 *  - Logout if last team overall
 *  - Delete team if last member
 *  - Prevent leaving if last coach but not last player
 */
object TeamLeaveLogic {

    suspend fun attemptLeaveTeam(
        team: Team,
        currentUserId: String,
        teamRepo: TeamRepository,
        userRepo: UserRepository
    ): String = withContext(Dispatchers.IO) {
        try {
            val allTeams = teamRepo.getTeamsForCurrentUser().first()
            val allUsers = userRepo.all().first()

            val isLastTeam = allTeams.size == 1
            val isLastPlayer = allUsers.size == 1
            val currentUser = allUsers.find { it.id == currentUserId }
            val isLastAdmin = allUsers.count { it.role == UserRole.COACH } == 1 &&
                    currentUser?.role == UserRole.COACH

            when {
                isLastTeam -> {
                    performLogout(teamRepo, userRepo)
                    "Leaving your last team. You have been logged out."
                }

                // ✅ Handle single-member team first
                isLastPlayer -> {
                    deleteTeamPermanently(team, teamRepo)
                    "You were the only player. Team ${team.name} deleted."
                }

                // Only then check for admin lockout
                isLastAdmin -> {
                    "You are the last coach. Assign another before leaving."
                }

                else -> {
                    executeLeave(team, teamRepo)
                    "Left team ${team.name}."
                }
            }
        } catch (e: Throwable) {
            Clogger.e("TeamLeaveLogic", "attemptLeaveTeam failed", e)
            "Error while leaving team: ${e.message}"
        }
    }

    private suspend fun executeLeave(team: Team, teamRepo: TeamRepository) {
        runCatching {
            teamRepo.delete(team)
            teamRepo.sync()
        }.onFailure { Clogger.e("TeamLeaveLogic", "executeLeave failed", it) }
    }

    private suspend fun deleteTeamPermanently(team: Team, teamRepo: TeamRepository) {
        runCatching {
            teamRepo.delete(team)
            teamRepo.sync()
        }.onFailure { Clogger.e("TeamLeaveLogic", "deleteTeamPermanently failed", it) }
    }

    private suspend fun performLogout(teamRepo: TeamRepository, userRepo: UserRepository) {
        runCatching {
            teamRepo.deleteAll()
            userRepo.deleteAll()
            FirebaseAuth.getInstance().signOut()
        }.onFailure { Clogger.e("TeamLeaveLogic", "performLogout failed", it) }
    }
}

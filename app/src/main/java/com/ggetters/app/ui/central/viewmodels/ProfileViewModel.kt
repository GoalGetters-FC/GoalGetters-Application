// app/src/main/java/com/ggetters/app/ui/central/viewmodels/ProfileViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ProfileViewModel"
    }

    /** App-wide active team (Room source-of-truth). */
    val activeTeam: StateFlow<Team?> =
        teamRepo.getActiveTeam()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Current logged-in user (snapshot at collection; safe and simple) */
    val currentUser: StateFlow<User?> =
        kotlinx.coroutines.flow.flow {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val user = if (uid != null) userRepo.getById(uid) else null
            emit(user)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Ensure user data is synced from remote on start so profile persists across restarts
        viewModelScope.launch {
            runCatching { userRepo.sync() }
                .onFailure { Clogger.e(TAG, "Failed to sync user data on init: ${it.message}", it) }
        }
    }

    fun logout() = authService.logout()
    
    /**
     * Update the current user's profile information
     */
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            try {
                userRepo.upsert(user)
                runCatching { userRepo.sync() }
                Clogger.d(TAG, "User profile updated successfully")
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to update user profile: ${e.message}", e)
            }
        }
    }
    
    /**
     * Delete the current user's account
     */
    fun deleteUserAccount() {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    val user = userRepo.getById(currentUserId)
                    if (user != null) {
                        userRepo.delete(user)
                        Clogger.d(TAG, "User account deleted successfully")
                    }
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Failed to delete user account: ${e.message}", e)
            }
        }
    }
}

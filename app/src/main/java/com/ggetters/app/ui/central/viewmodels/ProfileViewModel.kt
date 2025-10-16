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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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

    /** Current logged-in user */
    val currentUser: StateFlow<User?> = 
        flow { 
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                // Get user from local database first
                val localUser = userRepo.getById(currentUserId)
                emit(localUser)
            } else {
                emit(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() = authService.logout()
    
    /**
     * Update the current user's profile information
     */
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            try {
                userRepo.upsert(user)
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

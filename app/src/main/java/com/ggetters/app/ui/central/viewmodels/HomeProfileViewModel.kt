package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class HomeProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _player = MutableLiveData<User?>()
    val player: LiveData<User?> get() = _player

    /** Load by explicit app User.id (when provided) */
    fun loadPlayer(playerId: String) {
        viewModelScope.launch {
            val result = userRepository.getById(playerId)
            _player.value = result
        }
    }

    /** Load the currently logged-in Firebase user for the active team. */
    fun loadCurrentUser() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                _player.value = null
                return@launch
            }

            // 1) Many installs use app User.id == Firebase uid (your seeder does this)
            val byId = userRepository.getById(uid)
            if (byId != null) {
                _player.value = byId
                return@launch
            }

            // 2) Try repository helper, if implemented
            val byAuthLocal = runCatching { userRepository.getLocalByAuthId(uid) }.getOrNull()
            if (byAuthLocal != null) {
                _player.value = byAuthLocal
                return@launch
            }

            // 3) Fallback: scan active-team members and match on authId
            val allInTeam = runCatching { userRepository.all().first() }.getOrNull()
            _player.value = allInTeam?.firstOrNull { it.authId == uid }
        }
    }

    fun updatePlayer(player: User) {
        viewModelScope.launch {
            userRepository.upsert(player)
            _player.value = player
        }
    }

    fun deletePlayer(player: User) {
        viewModelScope.launch {
            userRepository.delete(player)
            _player.value = null
        }
    }


    fun currentUserId(): String? =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    fun resolveCurrentUserId(onResolved: (String?) -> Unit) {
        viewModelScope.launch {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) { onResolved(null); return@launch }

            // Try direct id match first
            userRepository.getById(uid)?.let { onResolved(it.id); return@launch }

            // Try local authId helper if your repo implements it
            runCatching { userRepository.getLocalByAuthId(uid) }.getOrNull()
                ?.let { onResolved(it.id); return@launch }

            // Fallback: scan active-team members for matching authId
            runCatching { userRepository.all().first() }.getOrNull()
                ?.firstOrNull { it.authId == uid }
                ?.let { onResolved(it.id) } ?: onResolved(null)
        }
    }

}

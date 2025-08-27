package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerDetailsViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _player = MutableStateFlow<User?>(null)
    val player: StateFlow<User?> = _player.asStateFlow()

    fun loadPlayer(playerId: String) {
        viewModelScope.launch {
            _player.value = userRepo.getById(playerId)
        }
    }

    fun updatePlayer(user: User) {
        viewModelScope.launch {
            userRepo.upsert(user)
            _player.value = user
        }
    }

    fun deletePlayer(user: User) {
        viewModelScope.launch {
            userRepo.delete(user)
            _player.value = null
        }
    }
}

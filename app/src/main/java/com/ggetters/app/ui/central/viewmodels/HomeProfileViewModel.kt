package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeProfileViewModel"
    }

    private val _player = MutableLiveData<User?>()
    val player: LiveData<User?> get() = _player

    fun loadPlayer(playerId: String) {
        viewModelScope.launch {
            val result = userRepository.getById(playerId)
            _player.value = result
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
}

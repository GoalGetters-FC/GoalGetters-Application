package com.ggetters.app.ui.views.user
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.ggetters.app.data.local.entities.UserEntity
//import com.ggetters.app.data.repository.UserRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
///**
// * Exposes user data to the UI and handles sync/save/delete actions.
// */
//@HiltViewModel
//class UserViewModel @Inject constructor(
//    private val userRepo: UserRepository
//) : ViewModel() {
//
//    // 1. Exposed list of all users (from local Room)
//    val users: StateFlow<List<UserEntity>> =
//        userRepo.observeAll()
//            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
//
//    // 2. Exposed detail of a single user by ID
//    private val _selectedUserId = MutableStateFlow<String?>(null)
//    val selectedUser: StateFlow<UserEntity?> = _selectedUserId
//        .filterNotNull()
//        .flatMapLatest { userRepo.observeById(it) }
//        .stateIn(viewModelScope, SharingStarted.Lazily, null)
//
//    // 3. UI state for errors or loading
//    private val _error = MutableSharedFlow<String>()
//    val errors: SharedFlow<String> = _error.asSharedFlow()
//
//    /** Call at screen start to sync remote → local */
//    fun syncUsers() = viewModelScope.launch {
//        try {
//            userRepo.syncAll()
//        } catch (e: Exception) {
//            _error.emit("Failed to sync users: ${e.message}")
//        }
//    }
//
//    /** Select which user the UI should display details for */
//    fun selectUser(id: String) {
//        _selectedUserId.value = id
//    }
//
//    /** Save or update a user (writes local then remote) */
//    fun saveUser(user: UserEntity) = viewModelScope.launch {
//        try {
//            userRepo.save(user)
//        } catch (e: Exception) {
//            _error.emit("Failed to save user: ${e.message}")
//        }
//    }
//
//    /** Delete a user */
//    fun deleteUser(id: String) = viewModelScope.launch {
//        try {
//            userRepo.delete(id)
//        } catch (e: Exception) {
//            _error.emit("Failed to delete user: ${e.message}")
//        }
//    }
//}

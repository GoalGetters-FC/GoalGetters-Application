// app/src/main/java/com/ggetters/app/ui/central/viewmodels/HomeSettingsViewModel.kt
package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthenticationService
import com.ggetters.app.data.remote.firestore.UserFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val authService: AuthenticationService,
    private val userFs: UserFirestore
) : ViewModel() {

    private val _fullName = MutableStateFlow<String?>(null)
    val fullName: StateFlow<String?> = _fullName.asStateFlow()

    init {
        val uid = getAuthAccount()?.uid
        if (uid != null) {
            viewModelScope.launch {
                userFs.observeFullNameForAuth(uid)
                    .catch { _fullName.value = null }  // prevent crashes on flow errors
                    .collect { name -> _fullName.value = name }
            }
        }
    }


    fun getAuthAccount() = authService.getCurrentUser()
    fun logout() = authService.logout()
}

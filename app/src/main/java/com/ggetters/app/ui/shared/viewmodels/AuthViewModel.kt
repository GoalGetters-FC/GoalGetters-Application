package com.ggetters.app.ui.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthorizationService
import com.ggetters.app.core.utils.Clogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    authorizationService: AuthorizationService
) : ViewModel() {
    companion object {
        const val TAG = "AuthViewModel"
    }


// --- Variables


    private val _isElevated = MutableStateFlow(false)
    val isElevatedBindable = _isElevated.asLiveData()
    val isElevated = _isElevated.asStateFlow()


    init {
        viewModelScope.launch {
            try {
                _isElevated.value = authorizationService.isCurrentUserElevated()
            } catch (e: Exception) {
                _isElevated.value = false
                Clogger.e(
                    TAG, "Failed to fetch current user privileges", e
                )
            }
        }
    }
}
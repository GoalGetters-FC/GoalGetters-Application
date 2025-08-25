package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import com.ggetters.app.core.services.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val authService: AuthenticationService
) : ViewModel() {
    companion object {
        private const val TAG = "HomeSettingsViewModel"
    }
    
    
// --- Functions
    
    
    
    fun getAuthAccount() = authService.getCurrentUser()
    
    
    fun logout() = authService.logout()
}
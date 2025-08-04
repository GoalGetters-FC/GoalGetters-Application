package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import com.ggetters.app.core.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    companion object {
        private const val TAG = "ProfileViewModel"
    }


// --- Contracts


    fun logout() = authService.logout()
}
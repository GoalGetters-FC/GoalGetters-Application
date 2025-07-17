package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.ui.startup.models.StartUiState
import com.ggetters.app.ui.startup.models.StartUiState.Authenticated
import com.ggetters.app.ui.startup.models.StartUiState.SignedOut
import com.google.firebase.auth.FirebaseAuth

class StartViewModel(
    private val authService: AuthService = AuthService(FirebaseAuth.getInstance())
) : ViewModel() {
    companion object {
        private const val TAG = "StartViewModel"
    }


// --- Fields


    private val _uiState = MutableLiveData<StartUiState>()
    val uiState: LiveData<StartUiState> = _uiState


// --- Contracts


    fun authenticate() {
        when (authService.isUserSignedIn()) {
            true -> _uiState.value = Authenticated
            else -> _uiState.value = SignedOut
        }
    }
}
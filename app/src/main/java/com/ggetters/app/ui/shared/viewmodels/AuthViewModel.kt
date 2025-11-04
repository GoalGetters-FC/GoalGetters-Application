package com.ggetters.app.ui.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.services.AuthorizationService
import com.ggetters.app.core.utils.Clogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val authorizationService: AuthorizationService
) : ViewModel() {
    companion object {
        const val TAG = "AuthViewModel"
    }


// --- Variables


    private val _isElevated = MutableStateFlow(false)
    val isElevatedBindable = _isElevated.asLiveData()


    init {
        viewModelScope.launch {
            authorizationService.userCollection.collect { _ ->
                checkElevationStatus()
            }
        }
    }


// --- Functions


    private suspend fun checkElevationStatus() {
        val elevated = try {
            authorizationService.isCurrentUserElevated()
        } catch (e: Exception) {
            Clogger.e(
                TAG, "Failed to fetch current user privileges", e
            )

            false
        }

        _isElevated.value = elevated
    }
}
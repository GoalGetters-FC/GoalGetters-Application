package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.ViewModel
import com.ggetters.app.core.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
}
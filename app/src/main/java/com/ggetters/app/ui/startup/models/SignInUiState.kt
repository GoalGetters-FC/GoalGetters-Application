package com.ggetters.app.ui.startup.models

sealed interface SignInUiState {
    object Success : SignInUiState
    object Loading : SignInUiState
    data class Failure(val message: String) : SignInUiState
}
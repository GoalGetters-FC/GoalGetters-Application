package com.ggetters.app.ui.startup.models

sealed interface SignUpUiState {
    object Success : SignUpUiState
    object Loading : SignUpUiState
    data class Failure(val message: String) : SignUpUiState
}
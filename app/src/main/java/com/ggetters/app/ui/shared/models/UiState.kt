package com.ggetters.app.ui.shared.models

sealed interface UiState {
    object Success : UiState
    object Loading : UiState
    data class Failure(val message: String) : UiState
}
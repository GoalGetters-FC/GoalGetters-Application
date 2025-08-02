package com.ggetters.app.ui.startup.models

sealed interface StartUiState {
    object Authenticated : StartUiState
    object SignedOut : StartUiState
}
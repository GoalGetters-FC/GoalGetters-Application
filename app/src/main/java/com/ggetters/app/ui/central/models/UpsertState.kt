package com.ggetters.app.ui.central.models

sealed class UpsertState {
    data object Idle : UpsertState()
    data object Saving : UpsertState()
    data class Saved(val id: String) : UpsertState()
    data class Error(val reason: String) : UpsertState()
}
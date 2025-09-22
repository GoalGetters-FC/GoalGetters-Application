package com.ggetters.app.core.services

data class Credential(
    val email: String,
    val token: String,
) {
    companion object {
        private const val TAG = "Credential"
    }
}
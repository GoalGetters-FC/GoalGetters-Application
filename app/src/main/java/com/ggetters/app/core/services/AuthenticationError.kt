package com.ggetters.app.core.services

import com.ggetters.app.core.models.results.FinalError

enum class AuthenticationError : FinalError {
    UNKNOWN, 
    TIMEOUT, 
    NETWORK, 
    INVALID_USER_STATUS, 
    INVALID_CREDENTIALS,
}
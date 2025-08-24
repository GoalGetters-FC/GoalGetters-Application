package com.ggetters.app.core.validation

import com.ggetters.app.core.models.results.FinalError

sealed interface ValidationError : FinalError {
    enum class String : ValidationError, FinalError {
        REQUIRE_MIN_CHARS,
        REQUIRE_MAX_CHARS,
        INCLUDE_LOWERCASE,
        EXCLUDE_LOWERCASE,
        INCLUDE_UPPERCASE,
        EXCLUDE_UPPERCASE,
        INCLUDE_WHITESPACE,
        EXCLUDE_WHITESPACE,
        INCLUDE_NUMBERS,
        EXCLUDE_NUMBERS,
        INCLUDE_LETTERS,
        EXCLUDE_LETTERS,
        INCLUDE_SYMBOLS,
        EXCLUDE_SYMBOLS,
        INCLUDE_CUSTOM_CHARACTERS,
        EXCLUDE_CUSTOM_CHARACTERS,
        INVALID_EMAIL_ADDRESS,
    }

    enum class Number : ValidationError, FinalError {
        UNKNOWN
    }
}
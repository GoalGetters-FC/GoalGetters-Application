package com.ggetters.app.core.models.results

typealias GenericError = FinalError

sealed interface Final<out D, out E : GenericError> {
    data class Success<out D, out E : GenericError>(val product: D) : Final<D, E>
    data class Failure<out D, out E : GenericError>(val problem: E) : Final<D, E>
}
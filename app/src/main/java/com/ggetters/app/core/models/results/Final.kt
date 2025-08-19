package com.ggetters.app.core.models.results

typealias GenericError = FinalError

sealed interface Final<out D, out E : GenericError> {
    data class Success<out D, out E : GenericError>(val product: D) : Final<D, E>
    data class Failure<out D, out E : GenericError>(val problem: E) : Final<D, E>


// --- Extensions
    

    fun onSuccess(execute: (D) -> Unit): Final<D, E> {
        if (this is Success) execute
        return this
    }


    fun onFailure(execute: (E) -> Unit): Final<D, E> {
        if (this is Failure) execute
        return this
    }
}
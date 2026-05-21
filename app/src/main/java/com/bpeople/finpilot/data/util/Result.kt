package com.bpeople.finpilot.data.util

/**
 * Simple Result wrapper for repository operations so ViewModels can observe Loading/Success/Error.
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
}


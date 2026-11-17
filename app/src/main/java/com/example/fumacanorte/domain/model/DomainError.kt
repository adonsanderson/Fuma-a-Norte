package com.example.fumacanorte.domain.model

sealed interface DomainError {
    data object NoInternet : DomainError
    data object Timeout : DomainError
    data class ServerError(
        val httpCode: Int,
        val serverMessage: String? = null
    ) : DomainError
    data object DataParsingError : DomainError
    data class Unknown(val causeMessage: String? = null) : DomainError
}

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

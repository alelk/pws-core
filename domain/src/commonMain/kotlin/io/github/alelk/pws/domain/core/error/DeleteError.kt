package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a delete operation. */
sealed interface DeleteError {
  data object NotFound : DeleteError
  data class ValidationError(val message: String) : DeleteError
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : DeleteError
}


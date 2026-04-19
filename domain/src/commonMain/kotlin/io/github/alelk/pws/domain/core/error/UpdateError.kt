package io.github.alelk.pws.domain.core.error

/** Errors that can occur during an update operation. */
sealed interface UpdateError {
  data object NotFound : UpdateError
  data class ValidationError(val message: String) : UpdateError
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpdateError
}


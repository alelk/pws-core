package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a create operation. */
sealed interface CreateError {
  data class AlreadyExists(val message: String = "Resource already exists") : CreateError
  data class ValidationError(val message: String) : CreateError
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : CreateError
}


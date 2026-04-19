package io.github.alelk.pws.domain.core.error

/** Errors that can occur during an upsert (insert-or-update) operation. */
sealed interface UpsertError {
  data class ValidationError(val message: String) : UpsertError
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpsertError
}


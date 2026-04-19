package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a toggle operation. */
sealed interface ToggleError {
  data object NotFound : ToggleError
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ToggleError
}


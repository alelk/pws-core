package io.github.alelk.pws.domain.core.error

/** Errors that can occur during an update operation. */
sealed interface UpdateError {
  val message: String

  data object NotFound : UpdateError {
    override val message: String = "Resource not found"
  }

  data class ValidationError(override val message: String) : UpdateError
  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpdateError
}

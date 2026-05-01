package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a read operation. */
sealed interface ReadError {
  val message: String

  data class NotFound(override val message: String = "Resource not found") : ReadError
  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ReadError
}

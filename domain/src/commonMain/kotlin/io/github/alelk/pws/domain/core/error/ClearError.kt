package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a clear-all operation. */
sealed interface ClearError {
  val message: String

  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ClearError
}

package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a toggle operation. */
sealed interface ToggleError {
  val message: String

  data object NotFound : ToggleError {
    override val message: String = "Resource not found"
  }

  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ToggleError
}

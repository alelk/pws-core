package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a replace-all operation. */
sealed interface ReplaceAllError {
  val message: String

  data class ValidationError(override val message: String) : ReplaceAllError
  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ReplaceAllError
}

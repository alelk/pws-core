package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a delete operation. */
sealed interface DeleteError {

  val message: String

  data object NotFound : DeleteError {
    override val message: String = "Resource not found"
  }

  data class ValidationError(override val message: String) : DeleteError
  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : DeleteError
}

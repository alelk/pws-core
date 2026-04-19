package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a create operation. */
sealed interface CreateError {
  val message: String

  data class AlreadyExists(override val message: String = "Resource already exists") : CreateError
  data class ValidationError(override val message: String) : CreateError
  data class UnknownError(
    val cause: Throwable? = null,
    override val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : CreateError
}

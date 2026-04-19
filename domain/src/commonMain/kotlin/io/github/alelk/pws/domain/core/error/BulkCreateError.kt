package io.github.alelk.pws.domain.core.error

/** Errors that can occur during a bulk-create operation. */
sealed interface BulkCreateError<out R : Any> {
  /** One or more resources in the input already exist. */
  data class AlreadyExists<out R : Any>(val resources: List<R>) : BulkCreateError<R>
  data class ValidationError(val message: String) : BulkCreateError<Nothing>
  data class UnknownError(
    val cause: Throwable? = null,
    val message: String = cause?.message ?: cause?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : BulkCreateError<Nothing>
}


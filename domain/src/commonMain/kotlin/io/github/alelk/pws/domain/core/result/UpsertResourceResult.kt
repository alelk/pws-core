package io.github.alelk.pws.domain.core.result

/**
 * Result of upserting (insert or update) a resource.
 * Unlike [UpdateResourceResult], NotFound is not possible here.
 */
sealed interface UpsertResourceResult<out R : Any> {
  /** Successfully upserted (created or updated). */
  data class Success<out R : Any>(val resource: R) : UpsertResourceResult<R>

  /** Validation error. */
  data class ValidationError<out R : Any>(val resource: R?, val message: String) : UpsertResourceResult<R>

  /** Unknown error occurred. */
  data class UnknownError<out R : Any>(
    val resource: R?,
    val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpsertResourceResult<R>
}

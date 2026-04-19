package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError

/** @deprecated Use [Either]<[DeleteError], R> instead. */
@Deprecated("Use Either<DeleteError, R> instead", level = DeprecationLevel.WARNING)
sealed interface DeleteResourceResult<out R : Any> {
  @Deprecated("Use Either.Right instead")
  data class Success<out R : Any>(val resource: R) : DeleteResourceResult<R>
  @Deprecated("Use Either.Left(DeleteError.NotFound) instead")
  data class NotFound<out R : Any>(val resource: R) : DeleteResourceResult<R>
  @Deprecated("Use Either.Left(DeleteError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(val resource: R, val message: String) : DeleteResourceResult<R>
  @Deprecated("Use Either.Left(DeleteError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    val resource: R, val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : DeleteResourceResult<R>
}

package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.UpsertError

/** @deprecated Use [Either]<[UpsertError], R> instead. */
@Deprecated("Use Either<UpsertError, R> instead", level = DeprecationLevel.WARNING)
sealed interface UpsertResourceResult<out R : Any> {
  @Deprecated("Use Either.Right instead")
  data class Success<out R : Any>(val resource: R) : UpsertResourceResult<R>
  @Deprecated("Use Either.Left(UpsertError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(val resource: R?, val message: String) : UpsertResourceResult<R>
  @Deprecated("Use Either.Left(UpsertError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    val resource: R?, val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpsertResourceResult<R>
}

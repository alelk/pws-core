package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.UpdateError

/** @deprecated Use [Either]<[UpdateError], R> instead. */
@Deprecated("Use Either<UpdateError, R> instead", level = DeprecationLevel.WARNING)
sealed interface UpdateResourceResult<out R : Any> {
  val resource: R
  @Deprecated("Use Either.Right instead")
  data class Success<out R : Any>(override val resource: R) : UpdateResourceResult<R>
  @Deprecated("Use Either.Left(UpdateError.NotFound) instead")
  data class NotFound<out R : Any>(override val resource: R) : UpdateResourceResult<R>
  @Deprecated("Use Either.Left(UpdateError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(override val resource: R, val message: String) : UpdateResourceResult<R>
  @Deprecated("Use Either.Left(UpdateError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    override val resource: R, val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpdateResourceResult<R>
}
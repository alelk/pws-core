@file:Suppress("DEPRECATION")

package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError

/**
 * @deprecated Use [Either]<[CreateError], R> instead.
 */
@Deprecated("Use Either<CreateError, R> instead", level = DeprecationLevel.WARNING)
sealed interface CreateResourceResult<out R : Any> {
  @Deprecated("Use Either.Right instead")
  data class Success<out R : Any>(val resource: R) : CreateResourceResult<R>

  @Deprecated("Use Either.Left(CreateError.AlreadyExists()) instead")
  data class AlreadyExists<out R : Any>(val resource: R, val message: String = "Resource already exists: $resource") : CreateResourceResult<R>

  @Deprecated("Use Either.Left(CreateError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(val resource: R, val message: String) : CreateResourceResult<R>

  @Deprecated("Use Either.Left(CreateError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    val resource: R,
    val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : CreateResourceResult<R>
}
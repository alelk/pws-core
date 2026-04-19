package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.BulkCreateError

/** @deprecated Use [Either]<[BulkCreateError]<R>, List<R>> instead. */
@Deprecated("Use Either<BulkCreateError<R>, List<R>> instead", level = DeprecationLevel.WARNING)
sealed interface CreateResourcesResult<out R : Any> {
  @Deprecated("Use Either.Right(resources) instead")
  data class Success<out R : Any>(val resources: List<R>) : CreateResourcesResult<R>
  @Deprecated("Use Either.Left(BulkCreateError.AlreadyExists(resources)) instead")
  data class AlreadyExists<out R : Any>(val resources: List<R>) : CreateResourcesResult<R>
  @Deprecated("Use Either.Left(BulkCreateError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(val resource: R, val message: String) : CreateResourcesResult<R>
  @Deprecated("Use Either.Left(BulkCreateError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    val resource: R, val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : CreateResourcesResult<R>
}
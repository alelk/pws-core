package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ReplaceAllError
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess

/** @deprecated Use [Either]<[ReplaceAllError], [ReplaceAllSuccess]<R>> instead. */
@Deprecated("Use Either<ReplaceAllError, ReplaceAllSuccess<R>> instead", level = DeprecationLevel.WARNING)
sealed interface ReplaceAllResourcesResult<out R : Any> {
  @Deprecated("Use ReplaceAllSuccess instead")
  data class Success<out R : Any>(val created: List<R>, val updated: List<R>, val unchanged: List<R>, val deleted: List<R>) : ReplaceAllResourcesResult<R>
  @Deprecated("Use Either.Left(ReplaceAllError.ValidationError(message)) instead")
  data class ValidationError<out R : Any>(val resource: R, val message: String) : ReplaceAllResourcesResult<R>
  @Deprecated("Use Either.Left(ReplaceAllError.UnknownError(exception)) instead")
  data class UnknownError<out R : Any>(
    val resource: R, val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : ReplaceAllResourcesResult<R>
}
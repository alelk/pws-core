package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.model.ToggleResult

/** @deprecated Use [Either]<[ToggleError], [ToggleResult]<R>> instead. */
@Deprecated("Use Either<ToggleError, ToggleResult<R>> instead", level = DeprecationLevel.WARNING)
sealed interface ToggleResourceResult<out R> {
  @Deprecated("Use ToggleResult.Enabled instead")
  data class Enabled<R>(val resource: R) : ToggleResourceResult<R>
  @Deprecated("Use ToggleResult.Disabled instead")
  data class Disabled<R>(val resource: R) : ToggleResourceResult<R>
  @Deprecated("Use Either.Left(ToggleError.NotFound) instead")
  data class NotFound<R>(val resource: R, val message: String = "Resource not found") : ToggleResourceResult<R>
  @Deprecated("Use Either.Left(ToggleError.UnknownError(exception)) instead")
  data class UnknownError(val exception: Throwable?, val message: String = exception?.message ?: "Unknown error") : ToggleResourceResult<Nothing>
}

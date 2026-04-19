package io.github.alelk.pws.domain.core.result

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError

/** @deprecated Use [Either]<[ClearError], Int> instead. */
@Deprecated("Use Either<ClearError, Int> instead", level = DeprecationLevel.WARNING)
sealed interface ClearResourcesResult {
  @Deprecated("Use Either.Right(removedCount) instead")
  data class Success(val removedCount: Int) : ClearResourcesResult
  @Deprecated("Use Either.Left(ClearError.UnknownError(exception)) instead")
  data class UnknownError(val exception: Throwable?, val message: String = exception?.message ?: "Unknown error") : ClearResourcesResult
}

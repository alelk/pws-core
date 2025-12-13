package io.github.alelk.pws.domain.core.result

/**
 * Result of clearing all resources from a collection.
 */
sealed interface ClearResourcesResult {
  /** Successfully cleared. */
  data class Success(val removedCount: Int) : ClearResourcesResult

  /** Unknown error occurred. */
  data class UnknownError(val exception: Throwable?, val message: String = exception?.message ?: "Unknown error") : ClearResourcesResult
}


package io.github.alelk.pws.domain.core.result

/**
 * Result of toggling a resource state.
 */
sealed interface ToggleResourceResult {
  /** Successfully toggled - resource is now active/added. */
  data object Enabled : ToggleResourceResult

  /** Successfully toggled - resource is now inactive/removed. */
  data object Disabled : ToggleResourceResult

  /** Resource not found. */
  data class NotFound(val message: String) : ToggleResourceResult

  /** Unknown error occurred. */
  data class UnknownError(val exception: Throwable?, val message: String = exception?.message ?: "Unknown error") : ToggleResourceResult
}


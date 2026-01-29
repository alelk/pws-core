package io.github.alelk.pws.domain.core.result

/**
 * Result of toggling a resource state.
 */
sealed interface ToggleResourceResult<out R> {
  /** Successfully toggled - resource is now active/added. */
  data class Enabled<R>(val resource: R) : ToggleResourceResult<R>

  /** Successfully toggled - resource is now inactive/removed. */
  data class Disabled<R>(val resource: R) : ToggleResourceResult<R>

  /** Resource not found. */
  data class NotFound<R>(val resource: R, val message: String = "Resource not found") : ToggleResourceResult<R>

  /** Unknown error occurred. */
  data class UnknownError(val exception: Throwable?, val message: String = exception?.message ?: "Unknown error") : ToggleResourceResult<Nothing>
}


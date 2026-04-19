package io.github.alelk.pws.domain.core.model

/** Result of a successful toggle operation. */
sealed interface ToggleResult<out R> {
  /** Resource is now enabled/added. */
  data class Enabled<out R>(val resource: R) : ToggleResult<R>

  /** Resource is now disabled/removed. */
  data class Disabled<out R>(val resource: R) : ToggleResult<R>
}


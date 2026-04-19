package io.github.alelk.pws.domain.core.model

/** Result of a successful replace-all operation. */
data class ReplaceAllSuccess<out R : Any>(
  val created: List<R>,
  val updated: List<R>,
  val unchanged: List<R>,
  val deleted: List<R>,
)


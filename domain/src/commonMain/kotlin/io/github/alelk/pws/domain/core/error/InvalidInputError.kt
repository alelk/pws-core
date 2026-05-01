package io.github.alelk.pws.domain.core.error

/** Validation error for parsing or constructing domain boundary input. */
data class InvalidInputError(
  val field: String,
  val message: String
)


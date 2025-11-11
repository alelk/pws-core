package io.github.alelk.pws.api.mapping.core

import io.github.alelk.pws.domain.core.NonEmptyString

internal fun nonEmpty(value: String, field: String): NonEmptyString {
  require(value.isNotBlank()) { "$field must not be blank" }
  return NonEmptyString(value)
}
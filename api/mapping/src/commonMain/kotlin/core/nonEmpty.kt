package io.github.alelk.pws.api.mapping.core

import io.github.alelk.pws.api.contract.core.error.FieldError
import io.github.alelk.pws.domain.core.NonEmptyString

/** Throws [IllegalArgumentException] if [value] is blank. Used by non-validated `toDomainCommand()`. */
fun nonEmpty(value: String, field: String): NonEmptyString {
  require(value.isNotBlank()) { "$field must not be blank" }
  return NonEmptyString(value)
}

/** Returns a [FieldError] if [value] is blank, or `null` if valid. Used by validated `toDomainCommandValidated()`. */
fun validateNonEmpty(value: String, field: String): FieldError? =
  if (value.isBlank()) FieldError(field, "must not be blank") else null

/** Returns a [FieldError] if the collection is empty, or `null` if valid. */
fun validateNonEmptyList(value: Collection<*>, field: String): FieldError? =
  if (value.isEmpty()) FieldError(field, "must not be empty") else null


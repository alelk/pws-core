package io.github.alelk.pws.api.contract.core.error

/** Common error codes used across the API. */
object ErrorCodes {
  // General validation errors
  const val VALIDATION_ERROR = "VALIDATION_ERROR"
  const val REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING"
  const val INVALID_FIELD_VALUE = "INVALID_FIELD_VALUE"

  // Resource conflicts
  const val ALREADY_EXISTS = "ALREADY_EXISTS"
  const val RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND"
  const val CONFLICT = "CONFLICT"

  // Authentication/Authorization
  const val UNAUTHORIZED = "UNAUTHORIZED"
  const val FORBIDDEN = "FORBIDDEN"

  // Server errors
  const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
  const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
}
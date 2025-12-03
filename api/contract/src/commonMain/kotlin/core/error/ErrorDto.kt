package io.github.alelk.pws.api.contract.core.error

import kotlinx.serialization.Serializable

/**
 * Standardized error response model for API contracts.
 * All API errors should return this structure with appropriate HTTP status codes.
 */
@Serializable
data class ErrorDto(
  val code: String,
  val message: String,
  val details: Map<String, String>? = null
) {
  companion object
}
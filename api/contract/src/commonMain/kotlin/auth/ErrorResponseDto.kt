package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
  val error: String,
  val message: String? = null
)


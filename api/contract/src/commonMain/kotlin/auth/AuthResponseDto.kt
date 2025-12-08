package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
  val accessToken: String,
  val refreshToken: String,
  val userId: String,
  val email: String,
  val username: String? = null,
  val role: String,
  val paymentStatus: String
)
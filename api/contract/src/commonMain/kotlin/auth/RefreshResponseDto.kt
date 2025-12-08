package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshResponseDto(
  val accessToken: String,
  val refreshToken: String
)

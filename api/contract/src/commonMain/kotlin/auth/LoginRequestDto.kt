package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
  val email: String,
  val password: String
)
package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
  val email: String,
  val password: String,
  val username: String? = null
)
package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
  val id: String,
  val email: String,
  val username: String? = null,
  val authProvider: String,
  val paymentStatus: String,
  val role: String,
  val createdAt: String,
  val updatedAt: String
)
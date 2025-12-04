package io.github.alelk.pws.api.mapping.auth

import io.github.alelk.pws.api.contract.auth.AuthResponseDto
import io.github.alelk.pws.api.contract.auth.UserResponseDto
import io.github.alelk.pws.domain.auth.model.UserDetail
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun UserDetail.toDto() = UserResponseDto(
  id = id.toString(),
  email = email,
  username = username,
  authProvider = authProvider.name,
  paymentStatus = accessPlan.name,
  role = role.name,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt.toString()
)

fun UserDetail.toDto(token: String) = AuthResponseDto(
  token = token,
  userId = id.toString(),
  email = email,
  username = username,
  role = role.name,
  paymentStatus = accessPlan.name
)
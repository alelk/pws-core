package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
  @SerialName("user")
  USER,

  @SerialName("admin")
  ADMIN
}


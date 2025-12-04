package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuthProvider {
  @SerialName("email")
  EMAIL,

  @SerialName("google")
  GOOGLE,

  @SerialName("vk")
  VK,

  @SerialName("telegram")
  TELEGRAM
}


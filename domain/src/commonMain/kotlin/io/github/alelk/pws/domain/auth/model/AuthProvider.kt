package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuthProvider(val identifier: String) {
  @SerialName("email")
  EMAIL("email"),

  @SerialName("google")
  GOOGLE("google"),

  @SerialName("vk")
  VK("vk"),

  @SerialName("telegram")
  TELEGRAM("telegram");

  companion object {
    fun fromIdentifier(identifier: String) =
      requireNotNull(entries.firstOrNull { it.identifier == identifier }) { "Unknown AuthProvider identifier: $identifier" }
  }
}


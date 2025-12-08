package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val identifier: String) {
  @SerialName("user")
  USER("user"),

  @SerialName("admin")
  ADMIN("admin");

  companion object {
    fun fromIdentifier(identifier: String) =
      requireNotNull(entries.firstOrNull { it.identifier == identifier }) { "Unknown UserRole identifier: $identifier" }
  }
}
package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccessPlan(val identifier: String) {
  @SerialName("free")
  FREE("free"),

  @SerialName("subscribed")
  SUBSCRIBED("subscribed"),

  @SerialName("full-version")
  FULL_VERSION("full-version");

  companion object {
    fun fromIdentifier(identifier: String) =
      requireNotNull(entries.firstOrNull { it.identifier == identifier }) { "Unknown AccessPlan identifier: $identifier" }
  }
}


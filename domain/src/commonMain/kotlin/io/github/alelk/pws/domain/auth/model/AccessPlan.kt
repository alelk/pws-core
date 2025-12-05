package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccessPlan {
  @SerialName("free")
  FREE,

  @SerialName("subscribed")
  SUBSCRIBED,

  @SerialName("full-version")
  FULL_VERSION
}


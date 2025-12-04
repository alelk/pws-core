package io.github.alelk.pws.domain.auth.model

import io.github.alelk.pws.domain.core.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
data class UserSummary(
  val id: UserId,
  val email: String,
  val username: String? = null,
  val accessPlan: AccessPlan,
  val role: UserRole
)


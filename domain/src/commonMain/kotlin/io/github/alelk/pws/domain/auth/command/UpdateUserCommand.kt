package io.github.alelk.pws.domain.auth.command

import io.github.alelk.pws.domain.auth.model.AccessPlan
import io.github.alelk.pws.domain.auth.model.UserRole
import io.github.alelk.pws.domain.core.ids.UserId

data class UpdateUserCommand(
  val id: UserId,
  val email: String? = null,
  val hashedPassword: String? = null,
  val username: String? = null,
  val accessPlan: AccessPlan? = null,
  val role: UserRole? = null,
  val profileJson: String? = null
) {
  fun hasChanges(): Boolean =
    email != null || hashedPassword != null || username != null || accessPlan != null || role != null || profileJson != null
}

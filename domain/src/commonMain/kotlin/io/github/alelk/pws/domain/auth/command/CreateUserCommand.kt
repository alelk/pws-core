package io.github.alelk.pws.domain.auth.command

import io.github.alelk.pws.domain.auth.model.AccessPlan
import io.github.alelk.pws.domain.auth.model.AuthProvider
import io.github.alelk.pws.domain.auth.model.UserRole
import io.github.alelk.pws.domain.core.ids.UserId

data class CreateUserCommand(
  val id: UserId,
  val email: String,
  val hashedPassword: String? = null,
  val authProvider: AuthProvider,
  val providerId: String? = null,
  val username: String? = null,
  val role: UserRole = UserRole.USER,
  val accessPlan: AccessPlan = AccessPlan.FREE,
  val profileJson: String? = null
)

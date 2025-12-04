package io.github.alelk.pws.domain.auth.model

import io.github.alelk.pws.domain.core.ids.UserId
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class UserDetail(
  val id: UserId,
  val email: String,
  val username: String? = null,
  val authProvider: AuthProvider,
  val providerId: String? = null,
  val paymentStatus: PaymentStatus = PaymentStatus.FREE,
  val role: UserRole = UserRole.USER,
  val profileJson: String? = null,
  val createdAt: Instant,
  val updatedAt: Instant
)


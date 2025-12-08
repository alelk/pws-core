package io.github.alelk.pws.domain.auth.model

import io.github.alelk.pws.domain.core.ids.UserId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class RefreshTokenDetail(
  val id: RefreshTokenId,
  val userId: UserId,
  val token: String,
  val expiresAt: Instant,
  val isRevoked: Boolean
)
package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.auth.model.RefreshTokenDetail
import io.github.alelk.pws.domain.auth.model.RefreshTokenId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Repository for managing refresh tokens stored in DB for revocation/rotation.
 */
@OptIn(ExperimentalTime::class)
interface RefreshTokenRepository {
  suspend fun insert(userId: UserId, token: String, expiresAt: Instant): CreateResourceResult<RefreshTokenId>
  suspend fun getByToken(token: String): RefreshTokenDetail?
  suspend fun getAllForUser(userId: UserId): List<RefreshTokenDetail>
  suspend fun revoke(token: String): Boolean
  suspend fun revokeAllForUser(userId: UserId): Int
}



package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.auth.model.UserDetail
import io.github.alelk.pws.domain.auth.model.AuthProvider

interface UserReadRepository {
  suspend fun get(id: UserId): UserDetail?
  suspend fun getByEmail(email: String): UserDetail?
  suspend fun getByProviderAndProviderId(provider: AuthProvider, providerId: String): UserDetail?
  suspend fun existsByEmail(email: String): Boolean
  suspend fun getHashedPassword(email: String): String?
}

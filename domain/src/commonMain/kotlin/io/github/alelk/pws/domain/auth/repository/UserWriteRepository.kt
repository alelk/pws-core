package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.auth.model.AuthProvider
import io.github.alelk.pws.domain.auth.model.PaymentStatus
import io.github.alelk.pws.domain.auth.model.UserDetail
import io.github.alelk.pws.domain.auth.model.UserRole
import io.github.alelk.pws.domain.core.ids.UserId

interface UserWriteRepository {
  suspend fun create(
    email: String,
    hashedPassword: String? = null,
    authProvider: AuthProvider,
    providerId: String? = null,
    username: String? = null,
    role: UserRole = UserRole.USER,
    paymentStatus: PaymentStatus = PaymentStatus.FREE,
    profileJson: String? = null
  ): UserDetail

  suspend fun update(
    id: UserId,
    email: String? = null,
    hashedPassword: String? = null,
    username: String? = null,
    paymentStatus: PaymentStatus? = null,
    role: UserRole? = null,
    profileJson: String? = null
  ): UserDetail?

  suspend fun delete(id: UserId): Boolean

  suspend fun linkTelegramAccount(userId: UserId, telegramProviderId: String): UserDetail?
}


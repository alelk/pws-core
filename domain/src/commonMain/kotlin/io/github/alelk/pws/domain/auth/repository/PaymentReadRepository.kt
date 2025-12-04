package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.auth.model.Payment
import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId

interface PaymentReadRepository {
  suspend fun get(id: PaymentId): Payment?
  suspend fun getByTransactionId(transactionId: String): Payment?
  suspend fun getAllByUserId(userId: UserId): List<Payment>
}


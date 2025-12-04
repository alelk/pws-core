package io.github.alelk.pws.domain.payment.repository

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.payment.model.Payment

interface PaymentReadRepository {
  suspend fun get(id: PaymentId): Payment?
  suspend fun getByTransactionId(transactionId: String): Payment?
  suspend fun getAllByUserId(userId: UserId): List<Payment>
}
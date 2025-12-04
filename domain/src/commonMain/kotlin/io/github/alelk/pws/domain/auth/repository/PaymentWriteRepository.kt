package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.auth.model.Payment
import io.github.alelk.pws.domain.auth.model.PaymentProvider
import io.github.alelk.pws.domain.auth.model.PaymentTransactionStatus
import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId

interface PaymentWriteRepository {
  suspend fun create(
    userId: UserId,
    provider: PaymentProvider,
    transactionId: String,
    amount: String,
    status: PaymentTransactionStatus = PaymentTransactionStatus.PENDING
  ): Payment

  suspend fun updateStatus(id: PaymentId, status: PaymentTransactionStatus): Payment?
}


package io.github.alelk.pws.domain.payment.repository

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.payment.model.Payment
import io.github.alelk.pws.domain.payment.model.PaymentProvider
import io.github.alelk.pws.domain.payment.model.PaymentTransactionStatus

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
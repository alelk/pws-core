package io.github.alelk.pws.domain.payment.command

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.payment.model.PaymentProvider
import io.github.alelk.pws.domain.payment.model.PaymentTransactionStatus

/** Command to create a Payment. */
data class CreatePaymentCommand(
  val id: PaymentId,
  val userId: UserId,
  val provider: PaymentProvider,
  val transactionId: String,
  val amount: String,
  val status: PaymentTransactionStatus = PaymentTransactionStatus.PENDING
)

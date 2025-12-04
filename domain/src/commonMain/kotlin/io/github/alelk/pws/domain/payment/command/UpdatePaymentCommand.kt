package io.github.alelk.pws.domain.payment.command

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.payment.model.PaymentTransactionStatus

/** Patch-like update for Payment fields. */
data class UpdatePaymentCommand(
  val id: PaymentId,
  val amount: String? = null,
  val status: PaymentTransactionStatus? = null
) {
  fun hasChanges(): Boolean = amount != null || status != null
}

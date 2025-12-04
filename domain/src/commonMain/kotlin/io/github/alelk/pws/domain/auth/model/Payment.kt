package io.github.alelk.pws.domain.auth.model

import io.github.alelk.pws.domain.core.ids.PaymentId
import io.github.alelk.pws.domain.core.ids.UserId
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class Payment(
  val id: PaymentId,
  val userId: UserId,
  val provider: PaymentProvider,
  val transactionId: String,
  val amount: String,
  val status: PaymentTransactionStatus,
  val timestamp: Instant
)


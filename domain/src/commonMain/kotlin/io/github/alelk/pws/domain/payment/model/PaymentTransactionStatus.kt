package io.github.alelk.pws.domain.payment.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentTransactionStatus {
  @SerialName("pending")
  PENDING,

  @SerialName("completed")
  COMPLETED,

  @SerialName("cancelled")
  CANCELLED,

  @SerialName("failed")
  FAILED
}
package io.github.alelk.pws.domain.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentProvider {
  @SerialName("rustore")
  RUSTORE,

  @SerialName("yoomoney")
  YOOMONEY,

  @SerialName("telegram-stars")
  TELEGRAM_STARS
}
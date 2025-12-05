package io.github.alelk.pws.domain.core.ids

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class PaymentId(val value: String) {
  init {
    require(value.isNotBlank()) { "PaymentId must not be blank" }
  }
  override fun toString(): String = value

  companion object {
    @OptIn(ExperimentalUuidApi::class)
    fun random(): PaymentId = PaymentId(Uuid.random().toString())
  }
}

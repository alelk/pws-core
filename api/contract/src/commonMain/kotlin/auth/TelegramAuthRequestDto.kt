package io.github.alelk.pws.api.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class TelegramAuthRequestDto(
  val initData: String
)
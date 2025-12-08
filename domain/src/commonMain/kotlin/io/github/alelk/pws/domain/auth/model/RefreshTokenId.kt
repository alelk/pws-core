package io.github.alelk.pws.domain.auth.model

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class RefreshTokenId(val value: String) {
  override fun toString(): String = value

  init {
    require(value.isNotBlank()) { "refresh token id is blank" }
  }

  companion object {
    @OptIn(ExperimentalUuidApi::class)
    fun random(): RefreshTokenId = RefreshTokenId(Uuid.random().toString())
  }
}
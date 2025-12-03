package io.github.alelk.pws.api.contract.core.ids

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class BookIdDto(val value: String) {
  override fun toString(): String = value

  companion object {
    fun parse(value: String): BookIdDto = BookIdDto(value)
  }
}
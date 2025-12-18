package io.github.alelk.pws.api.contract.core

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Color in hex format (e.g., "#FF0000" for red).
 */
@JvmInline
@Serializable
value class ColorDto(val value: String) {
  init {
    require(colorPattern.matches(value)) { "Invalid color format: '$value'. Expected format: #RRGGBB" }
  }

  override fun toString(): String = value

  companion object {
    private val colorPattern = Regex("^#([0-9A-Fa-f]{6})$")

    fun parse(value: String): ColorDto = ColorDto(value)
  }
}


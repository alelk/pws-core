package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed interface LyricPartDto {
  val numbers: Set<Int>
  val text: String

  @Serializable
  @SerialName("chorus")
  data class Chorus(override val numbers: Set<Int>, override val text: String) : LyricPartDto

  @Serializable
  @SerialName("verse")
  data class Verse(override val numbers: Set<Int>, override val text: String) : LyricPartDto

  @Serializable
  @SerialName("bridge")
  data class Bridge(override val numbers: Set<Int>, override val text: String) : LyricPartDto
}
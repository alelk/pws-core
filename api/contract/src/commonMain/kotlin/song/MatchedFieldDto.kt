package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchedFieldDto {
  @SerialName("name")
  NAME,

  @SerialName("lyric")
  LYRIC
}
package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable

@Serializable
enum class MatchedFieldDto {
  NAME,
  LYRIC
}
package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable

@Serializable
enum class SearchTypeDto {
  ALL,
  NAME,
  LYRIC,
  NUMBER
}
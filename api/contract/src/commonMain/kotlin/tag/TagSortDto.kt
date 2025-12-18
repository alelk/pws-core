package io.github.alelk.pws.api.contract.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TagSortDto {
  @SerialName("name") ByName,
  @SerialName("priority") ByPriority,
  @SerialName("song_count") BySongCount
}


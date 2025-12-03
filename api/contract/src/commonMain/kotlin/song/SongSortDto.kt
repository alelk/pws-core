package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SongSortDto {
  @SerialName("id")
  ById,

  @SerialName("id-desc")
  ByIdDesc,

  @SerialName("name")
  ByName,

  @SerialName("name-desc")
  ByNameDesc,

  @SerialName("number")
  ByNumber,

  @SerialName("number-desc")
  ByNumberDesc
}

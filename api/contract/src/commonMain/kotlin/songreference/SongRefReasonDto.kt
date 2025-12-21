package io.github.alelk.pws.api.contract.songreference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SongRefReasonDto {
  @SerialName("variation")
  VARIATION
}

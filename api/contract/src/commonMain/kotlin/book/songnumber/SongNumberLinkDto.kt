package io.github.alelk.pws.api.contract.book.songnumber

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongNumberLinkDto(val songId: SongIdDto, val number: Int) {
  init {
    require(number > 0) { "song number should be greater than 0: $number" }
    require(number < 1_000_000) { "song number should be less than 1 000 000: $number" }
  }
}
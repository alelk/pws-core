package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongNumberDto(val bookId: BookIdDto, val number: Int) {
  init {
    require(number > 0) { "song number should be greater than 0: $number" }
    require(number < 1_000_000) { "song number should be less than 1 000 000: $number" }
  }
}


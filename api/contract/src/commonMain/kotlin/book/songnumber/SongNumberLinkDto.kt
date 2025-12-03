package io.github.alelk.pws.api.contract.book.songnumber

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

@Serializable
data class SongNumberLinkDto(val songId: SongIdDto, val number: Int) {
  init {
    require(number > 0) { "song number should be greater than 0: $number" }
    require(number < 1_000_000) { "song number should be less than 1 000 000: $number" }
  }

  override fun toString(): String = "$number>$songId"

  companion object {
    fun parse(string: String): SongNumberLinkDto =
      runCatching {
        val (number, songId) = string.split('>')
        SongNumberLinkDto(SongIdDto(songId.toLong()), number.toInt())
      }.getOrElse { e ->
        throw IllegalArgumentException("unable to parse song number link from string '$string': expected format 'number>songId': ${e.message}", e)
      }
  }
}
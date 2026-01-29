package io.github.alelk.pws.api.contract.history

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Identifies what was viewed in history.
 *
 * Songs can be viewed either:
 * - As part of a book (with book context and song number)
 * - As a standalone song (without book association)
 */
@Serializable
sealed interface HistorySubjectDto {

  /**
   * A song viewed in context of a specific book.
   */
  @Serializable
  @SerialName("booked")
  data class BookedSong(
    val songNumberId: SongNumberIdDto
  ) : HistorySubjectDto

  /**
   * A standalone song viewed without book context.
   */
  @Serializable
  @SerialName("standalone")
  data class StandaloneSong(
    val songId: SongIdDto
  ) : HistorySubjectDto
}

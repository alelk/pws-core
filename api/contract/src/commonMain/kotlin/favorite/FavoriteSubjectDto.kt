package io.github.alelk.pws.api.contract.favorite

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Identifies what is favorited.
 */
@Serializable
sealed interface FavoriteSubjectDto {

  /**
   * A song favorited in context of a specific book.
   */
  @Serializable
  @SerialName("booked")
  data class BookedSong(val songNumberId: SongNumberIdDto) : FavoriteSubjectDto

  /**
   * A standalone song favorited without book context.
   */
  @Serializable
  @SerialName("standalone")
  data class StandaloneSong(val songId: SongIdDto) : FavoriteSubjectDto
}

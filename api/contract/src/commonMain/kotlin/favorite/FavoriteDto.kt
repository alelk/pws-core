package io.github.alelk.pws.api.contract.favorite

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Favorite entry response with full song info for display.
 */
@Serializable
sealed interface FavoriteDto {
  val songName: String
  @OptIn(ExperimentalTime::class)
  val addedAt: Instant

  /**
   * A song favorited in context of a specific book.
   */
  @Serializable
  @SerialName("booked")
  data class BookedSong @OptIn(ExperimentalTime::class) constructor(
    val songNumberId: SongNumberIdDto,
    val songNumber: Int,
    val bookDisplayName: String,
    override val songName: String,
    override val addedAt: Instant
  ) : FavoriteDto

  /**
   * A standalone song favorited without book context.
   */
  @Serializable
  @SerialName("standalone")
  data class StandaloneSong @OptIn(ExperimentalTime::class) constructor(
    val songId: SongIdDto,
    override val songName: String,
    override val addedAt: Instant
  ) : FavoriteDto
}


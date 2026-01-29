package io.github.alelk.pws.api.contract.history

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * History entry DTO with full song information.
 *
 * Supports two types of entries:
 * - [BookedSong]: A song viewed in context of a specific book
 * - [StandaloneSong]: A song viewed without book context
 */
@Serializable
sealed interface HistoryEntryDto {

  /** When the song was last viewed (ISO 8601 format). */
  @OptIn(ExperimentalTime::class)
  val viewedAt: Instant

  /** How many times the song was viewed. */
  val viewCount: Int

  /** Name of the song. */
  val songName: String

  /**
   * A song viewed in context of a specific book.
   * Note: bookId and songId can be extracted from songNumberId on the client side.
   */
  @Serializable
  @SerialName("booked")
  data class BookedSong @OptIn(ExperimentalTime::class) constructor(
    val songNumberId: SongNumberIdDto,
    val songNumber: Int,
    val bookDisplayName: String,
    override val songName: String,
    override val viewedAt: Instant,
    override val viewCount: Int
  ) : HistoryEntryDto

  /**
   * A song viewed without book context.
   */
  @Serializable
  @SerialName("standalone")
  data class StandaloneSong @OptIn(ExperimentalTime::class) constructor(
    val songId: SongIdDto,
    override val songName: String,
    override val viewedAt: Instant,
    override val viewCount: Int
  ) : HistoryEntryDto
}

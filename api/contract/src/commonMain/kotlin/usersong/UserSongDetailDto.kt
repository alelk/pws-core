package io.github.alelk.pws.api.contract.usersong

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.TonalityDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.YearDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.LyricDto
import kotlinx.serialization.Serializable

/**
 * Song with user's overrides applied (merged view).
 *
 * This DTO provides:
 * - The effective (merged) song data
 * - Metadata about user's overrides
 */
@Serializable
data class UserSongDetailDto(
  val id: SongIdDto,
  val version: VersionDto,
  val locale: LocaleDto,
  val name: String,
  val lyric: LyricDto,
  val author: PersonDto?,
  val translator: PersonDto? = null,
  val composer: PersonDto? = null,
  val tonalities: List<TonalityDto>? = null,
  val year: YearDto? = null,
  val bibleRef: String? = null,

  /** Source of this song: GLOBAL or USER */
  val source: SongSourceDto,

  /** Whether user has any overrides applied to this song */
  val hasOverride: Boolean = false,

  /** List of field names that are overridden by user */
  val overriddenFields: List<String> = emptyList()
)

/**
 * Source type of the song.
 */
@Serializable
enum class SongSourceDto {
  /** Global song managed by administrators */
  GLOBAL,
  /** User-created song in user's custom book */
  USER
}


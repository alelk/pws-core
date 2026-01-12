package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.tonality.Tonality

/**
 * Song with user's overrides applied (merged view).
 *
 * Provides:
 * - Effective (merged) song data
 * - Metadata about user's overrides
 */
data class MergedSongDetail(
  val id: SongId,
  val version: Version,
  val locale: Locale,
  val name: NonEmptyString,
  val lyric: Lyric,
  val author: Person? = null,
  val translator: Person? = null,
  val composer: Person? = null,
  val tonalities: List<Tonality>? = null,
  val year: Year? = null,
  val bibleRef: BibleRef? = null,

  /** Source of this song */
  val source: SongSource,

  /** Whether user has any overrides applied */
  val hasOverride: Boolean = false,

  /** Fields that are overridden by user */
  val overriddenFields: Set<SongField> = emptySet()
)

/**
 * Source type of the song.
 */
enum class SongSource {
  /** Global song managed by administrators */
  GLOBAL,
  /** User-created song in user's custom book */
  USER
}

/**
 * Song fields that can be overridden.
 */
enum class SongField {
  NAME,
  LYRIC,
  AUTHOR,
  TRANSLATOR,
  COMPOSER,
  TONALITIES,
  BIBLE_REF
}


package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.tonality.Tonality

/**
 * Summary of user's song for list views.
 */
data class MergedSongSummary(
  val id: SongId,
  val locale: Locale,
  val name: NonEmptyString,
  val author: Person? = null,
  val tonalities: List<Tonality>? = null,

  /** Source of this song */
  val source: SongSource,

  /** Whether user has any overrides applied */
  val hasOverride: Boolean = false
)


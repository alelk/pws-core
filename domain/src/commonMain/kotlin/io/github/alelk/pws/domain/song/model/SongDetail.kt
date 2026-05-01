package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.tonality.Tonality
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.core.getOrElse

/** Detailed song view. */
data class SongDetail(
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
  val edited: Boolean = false,
)

/** Apply [UpdateSongCommand] to this [SongDetail]. */
fun SongDetail.apply(command: UpdateSongCommand): SongDetail =
  copy(
    version = command.version ?: version,
    locale = command.locale ?: locale,
    name = command.name ?: name,
    lyric = command.lyric ?: lyric,
    author = command.author.getOrElse { author },
    translator = command.translator.getOrElse { translator },
    composer = command.composer.getOrElse { composer },
    tonalities = command.tonalities.getOrElse { tonalities },
    year = command.year.getOrElse { year },
    bibleRef = command.bibleRef.getOrElse { bibleRef },
    edited = true
  )
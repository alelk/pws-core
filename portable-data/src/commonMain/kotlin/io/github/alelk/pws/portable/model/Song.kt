package io.github.alelk.pws.portable.model

import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.YearSerializer
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.tonality.Tonality
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.song.model.BibleRefSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Song @OptIn(ExperimentalSerializationApi::class) constructor(
  /**
   * Primary book-number of the song (the book/number where the song "lives").
   *
   * **Compatibility note:** This field exists in all YAML versions (v1+) and must remain present
   * for backward-compatible reading of old backup and bundle files.
   * For songs that belong to multiple books use [numbers]; [allNumbers] always returns the
   * full deduplicated set.
   */
  val number: SongNumber,
  val id: SongId? = null,
  val version: Version,
  val locale: Locale,
  val name: String,
  val lyric: String,
  /**
   * Additional book-numbers for deduplicated songs that appear in more than one book.
   *
   * **Compatibility note:** Absent in old YAML backups — deserialises as empty list.
   * [number] always holds the primary (first) number; [numbers] holds the *rest*.
   * Use [allNumbers] to get all numbers including [number].
   */
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  val numbers: List<SongNumber> = emptyList(),
  val tonalities: List<Tonality>? = null,
  val author: Person? = null,
  val translator: Person? = null,
  val composer: Person? = null,
  @Serializable(with = BibleRefSerializer::class)
  val bibleRef: BibleRef? = null,
  // Fields added in portable-data v2 — absent in old Backup YAML, use defaults.
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  @Serializable(with = YearSerializer::class)
  val year: Year? = null,
  @EncodeDefault(EncodeDefault.Mode.NEVER)
  val priority: Int = 0,
) {
  /**
   * All book-numbers for this song: [number] + [numbers], deduplicated, preserving order.
   *
   * Always contains at least one element ([number]).
   * Use this instead of [number] when you need to handle multi-book songs.
   */
  @Transient
  val allNumbers: List<SongNumber> = (listOf(number) + numbers).distinct()

  init {
    val dupes = allNumbers.groupBy { it.bookId }.filterValues { it.size > 1 }.keys
    require(dupes.isEmpty()) {
      "song $id contains multiple numbers for the same book: ${dupes.joinToString()}"
    }
  }
}

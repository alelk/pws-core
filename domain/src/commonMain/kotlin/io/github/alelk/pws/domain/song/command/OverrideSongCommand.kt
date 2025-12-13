package io.github.alelk.pws.domain.song.command

import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.tonality.Tonality

/**
 * Command to override a global song for a specific user.
 * All fields are optional - only non-null fields will be applied as overrides.
 */
data class OverrideSongCommand(
  val songId: SongId,
  val name: NonEmptyString? = null,
  val lyric: Lyric? = null,
  val author: OptionalField<Person?> = OptionalField.Unchanged,
  val translator: OptionalField<Person?> = OptionalField.Unchanged,
  val composer: OptionalField<Person?> = OptionalField.Unchanged,
  val bibleRef: OptionalField<BibleRef?> = OptionalField.Unchanged,
  val tonalities: OptionalField<List<Tonality>?> = OptionalField.Unchanged
) {
  fun hasChanges(): Boolean =
    name != null ||
      lyric != null ||
      author !is OptionalField.Unchanged ||
      translator !is OptionalField.Unchanged ||
      composer !is OptionalField.Unchanged ||
      bibleRef !is OptionalField.Unchanged ||
      tonalities !is OptionalField.Unchanged
}


package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.bookId
import io.github.alelk.pws.domain.core.ids.songId
import io.github.alelk.pws.domain.core.nonEmptyString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.songBookReference(
  bookId: Arb<BookId> = Arb.bookId(),
  displayShortName: Arb<NonEmptyString> = Arb.nonEmptyString(2..5),
  songNumber: Arb<Int> = Arb.int(1..999)
): Arb<SongBookReference> =
  arbitrary {
    SongBookReference(
      bookId = bookId.bind(),
      displayShortName = displayShortName.bind(),
      songNumber = songNumber.bind()
    )
  }

fun Arb.Companion.songSearchSuggestion(
  id: Arb<SongId> = Arb.songId(),
  name: Arb<NonEmptyString> = Arb.nonEmptyString(1..40),
  bookReferences: Arb<List<SongBookReference>> = Arb.list(Arb.songBookReference(), 0..3),
  snippet: Arb<String?> = Arb.string(10..100).orNull()
): Arb<SongSearchSuggestion> =
  arbitrary {
    SongSearchSuggestion(
      id = id.bind(),
      name = name.bind(),
      bookReferences = bookReferences.bind(),
      snippet = snippet.bind()
    )
  }


package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.songId
import io.github.alelk.pws.domain.core.nonEmptyString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.songSearchSuggestion(
  id: Arb<SongId> = Arb.songId(),
  name: Arb<NonEmptyString> = Arb.nonEmptyString(1..40),
  books: Arb<List<String>> = Arb.list(Arb.string(3..10), 1..3),
  snippet: Arb<String?> = Arb.string(10..100).orNull()
): Arb<SongSearchSuggestion> =
  arbitrary {
    SongSearchSuggestion(
      id = id.bind(),
      name = name.bind(),
      books = books.bind(),
      snippet = snippet.bind()
    )
  }


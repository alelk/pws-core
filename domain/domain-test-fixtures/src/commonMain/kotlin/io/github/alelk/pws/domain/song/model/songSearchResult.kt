package io.github.alelk.pws.domain.song.model

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string

fun Arb.Companion.songSearchResult(
  song: Arb<SongSummary> = Arb.songSummary(),
  snippet: Arb<String> = Arb.string(20..200),
  rank: Arb<Float> = Arb.float(0f..1f),
  matchedFields: Arb<List<MatchedField>> = Arb.list(Arb.element(MatchedField.NAME, MatchedField.LYRIC), 1..2)
): Arb<SongSearchResult> =
  arbitrary {
    SongSearchResult(
      song = song.bind(),
      snippet = snippet.bind(),
      rank = rank.bind(),
      matchedFields = matchedFields.bind()
    )
  }


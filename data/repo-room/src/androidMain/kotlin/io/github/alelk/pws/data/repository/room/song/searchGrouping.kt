package io.github.alelk.pws.data.repository.room.song

import io.github.alelk.pws.database.song.SongSearchResultEntity
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.song.model.SongBookReference
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion

/**
 * Collapses raw FTS rows into autocomplete suggestions: one [SongSearchSuggestion] per song, with
 * all of that song's book references merged and the first non-blank snippet kept.
 *
 * Pure (no DB access) so it can be unit-tested without Room.
 */
fun groupSuggestions(results: List<SongSearchResultEntity>): List<SongSearchSuggestion> =
  results
    .groupBy { it.songId }
    .map { (songId, rows) ->
      SongSearchSuggestion(
        id = songId,
        name = NonEmptyString(rows.first().songName),
        bookReferences = rows.map { r ->
          SongBookReference(
            bookId = r.bookId,
            displayShortName = NonEmptyString(r.bookDisplayName),
            songNumber = r.songNumber
          )
        },
        snippet = rows.firstOrNull { it.snippet.isNotBlank() }?.snippet
      )
    }

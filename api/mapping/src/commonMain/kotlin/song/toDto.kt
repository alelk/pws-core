package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.song.LyricDto
import io.github.alelk.pws.api.contract.song.LyricPartDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.SearchTypeDto
import io.github.alelk.pws.api.contract.song.MatchedFieldDto
import io.github.alelk.pws.api.contract.song.SongBookReferenceDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestionDto
import io.github.alelk.pws.api.contract.song.SongSearchResultDto
import io.github.alelk.pws.api.contract.song.SongSearchResponseDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.query.SearchType
import io.github.alelk.pws.domain.song.model.MatchedField
import io.github.alelk.pws.domain.song.model.SongBookReference
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.model.SongSearchResult
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.lyric.Bridge
import io.github.alelk.pws.domain.song.lyric.Chorus
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.LyricPart
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.tonality.Tonality

fun SongSummary.toDto(): SongSummaryDto = SongSummaryDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  edited = edited
)

fun LyricPart.toDto(): LyricPartDto = when (this) {
  is Chorus -> LyricPartDto.Chorus(numbers, text)
  is Verse -> LyricPartDto.Verse(numbers, text)
  is Bridge -> LyricPartDto.Bridge(numbers, text)
}

fun Lyric.toDto(): LyricDto = LyricDto(map { it.toDto() })

fun SongDetail.toDto(): SongDetailDto = SongDetailDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  lyric = lyric.toDto(),
  author = author?.toDto(),
  translator = translator?.toDto(),
  composer = composer?.toDto(),
  tonalities = tonalities?.map(Tonality::toDto),
  year = year?.toDto(),
  bibleRef = bibleRef?.text,
  edited = edited
)

fun SongSort.toDto(): SongSortDto = when (this) {
  SongSort.ById -> SongSortDto.ById
  SongSort.ByIdDesc -> SongSortDto.ByIdDesc
  SongSort.ByName -> SongSortDto.ByName
  SongSort.ByNameDesc -> SongSortDto.ByNameDesc
  SongSort.ByNumber -> SongSortDto.ByNumber
  SongSort.ByNumberDesc -> SongSortDto.ByNumberDesc
}

fun SearchType.toDto(): SearchTypeDto = when (this) {
  SearchType.ALL -> SearchTypeDto.ALL
  SearchType.NAME -> SearchTypeDto.NAME
  SearchType.LYRIC -> SearchTypeDto.LYRIC
  SearchType.NUMBER -> SearchTypeDto.NUMBER
}

fun MatchedField.toDto(): MatchedFieldDto = when (this) {
  MatchedField.NAME -> MatchedFieldDto.NAME
  MatchedField.LYRIC -> MatchedFieldDto.LYRIC
}

fun SongBookReference.toDto(): SongBookReferenceDto = SongBookReferenceDto(
  bookId = bookId.toDto(),
  displayShortName = displayShortName.value,
  songNumber = songNumber
)

fun SongSearchSuggestion.toDto(): SongSearchSuggestionDto = SongSearchSuggestionDto(
  id = id.toDto(),
  name = name.value,
  bookReferences = bookReferences.map { it.toDto() },
  snippet = snippet
)

fun SongSearchResult.toDto(): SongSearchResultDto = SongSearchResultDto(
  song = song.toDto(),
  snippet = snippet,
  rank = rank,
  matchedFields = matchedFields.map { it.toDto() },
  bookReferences = bookReferences.map { it.toDto() }
)

fun SongSearchResponse.toDto(): SongSearchResponseDto = SongSearchResponseDto(
  results = results.map { it.toDto() },
  totalCount = totalCount,
  hasMore = hasMore
)


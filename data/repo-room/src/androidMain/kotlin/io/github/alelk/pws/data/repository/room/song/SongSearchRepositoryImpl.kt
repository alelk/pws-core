package io.github.alelk.pws.data.repository.room.song

import io.github.alelk.pws.database.song.SongDao
import io.github.alelk.pws.database.song.SongSearchResultEntity
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.model.SongBookReference
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.model.SongSearchResult
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.model.MatchedField
import io.github.alelk.pws.domain.song.query.SearchQuery
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

class SongSearchRepositoryImpl(
  private val songDao: SongDao
) : SongSearchRepository {

  override suspend fun searchSuggestions(
    query: String,
    userId: UserId?,
    bookId: BookId?,
    limit: Int
  ): List<SongSearchSuggestion> {
    val trimmed = query.trim()
    val raw = trimmed.toIntOrNull()
    val results: List<SongSearchResultEntity> = if (raw != null) {
      songDao.findBySongNumber(raw, limit)
    } else {
      songDao.findBySongText(buildFtsPrefixQuery(trimmed), limit)
    }

    // Group by songId to merge multiple book references
    return groupSuggestions(results)
  }

  override suspend fun search(
    searchQuery: SearchQuery,
    userId: UserId?,
    bookId: BookId?
  ): SongSearchResponse {
    val trimmed = searchQuery.query.trim()
    val raw = trimmed.toIntOrNull()
    val results: List<SongSearchResultEntity> = if (raw != null) {
      songDao.findBySongNumber(raw, searchQuery.limit)
    } else {
      songDao.findBySongText(buildFtsPrefixQuery(trimmed), searchQuery.limit)
    }

    // Fetch full song data for SongSummary
    val songIds = results.map { it.songId }.distinct()
    val songs = songDao.getByIds(songIds).associateBy { it.id }

    val searchResults = results.mapNotNull { r ->
      val songEntity = songs[r.songId] ?: return@mapNotNull null
      SongSearchResult(
        song = songEntity.toSummary(),
        snippet = r.snippet,
        rank = 1f,
        matchedFields = emptyList(),
        bookReferences = listOf(
          SongBookReference(bookId = r.bookId, displayShortName = NonEmptyString(r.bookDisplayName), songNumber = r.songNumber)
        )
      )
    }
    return SongSearchResponse(results = searchResults, totalCount = searchResults.size.toLong(), hasMore = false)
  }
}



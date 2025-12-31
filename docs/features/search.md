# Song Search

## Description

Full-text search across songbook songs.

## Search Options

### By Songbook Number
- Input: `123` or `GS 45`
- Search by `SongNumber.number` field
- Exact number match

### By Text
- Input: arbitrary text
- Search by:
  - Song title
  - Lyrics
- Match highlighting in results

## Use Cases

### SearchSongsUseCase
```kotlin
class SearchSongsUseCase(
    private val searchRepository: SongSearchRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(
        searchQuery: SearchQuery,
        userId: UserId? = null,
        bookId: BookId? = null
    ): SongSearchResponse = txRunner.inRoTransaction {
        // If scope is USER_BOOKS but no userId, return empty result
        if (searchQuery.scope == SearchScope.USER_BOOKS && userId == null)
            SongSearchResponse(emptyList(), 0, false)
        else
            searchRepository.search(searchQuery, userId, bookId)
    }
}
```

### SearchSongSuggestionsUseCase
```kotlin
class SearchSongSuggestionsUseCase(
    private val searchRepository: SongSearchRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(
        query: String,
        userId: UserId? = null,
        bookId: BookId? = null,
        limit: Int = 10
    ): List<SongSearchSuggestion> =
        txRunner.inRoTransaction {
            searchRepository.searchSuggestions(query, userId, bookId, limit)
        }
}
```

## Models

### SongSearchResult
```kotlin
data class SongSearchResult(
    val song: SongSummary,
    val snippet: String,
    val rank: Float,
    val matchedFields: List<MatchedField>
)
```

### SongSearchSuggestion
```kotlin
data class SongSearchSuggestion(
    val id: SongId,
    val name: NonEmptyString,
    val books: List<String>,
    val snippet: String? = null
)
```

### MatchedField
```kotlin
enum class MatchedField {
    NAME,
    LYRIC
}
```

## UI Flow

```
┌─────────────────────────────────────────────┐
│              SearchScreen                   │
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐    │
│  │  🔍 Search songs...                 │    │  ◀── TextField
│  └─────────────────────────────────────┘    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Suggestions:                       │    │  ◀── Suggestions
│  │  • Blessed Be the Lord              │    │      (appear while typing)
│  │  • GS 123                           │    │
│  └─────────────────────────────────────┘    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Results:                           │    │  ◀── Search Results
│  │  ┌───────────────────────────────┐  │    │
│  │  │ GS 45 - Blessed Be the Lord   │  │    │
│  │  │ "...Blessed be the Lord..."   │  │    │
│  │  └───────────────────────────────┘  │    │
│  │  ┌───────────────────────────────┐  │    │
│  │  │ SV 12 - Grace                 │  │    │
│  │  │ "...blessed day..."           │  │    │
│  │  └───────────────────────────────┘  │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

## Search Implementations

### Local (Room FTS5)
- Uses FTS5 for full-text search
- Indexes on title, lyrics
- Fast search without internet

### Remote (Exposed)
- Backend uses Exposed queries in PostgreSQL
- Requires internet

## Debounce

When typing text, 300ms debounce is used for suggestions.
When tapping a suggestion, the selected song opens.
When pressing Enter or search button, song search is performed and results are displayed.

## Related Files

- `domain/song/usecase/SearchSongsUseCase.kt`
- `domain/song/usecase/SearchSongSuggestionsUseCase.kt`
- `domain/song/model/SongSearchResult.kt`
- `domain/song/repository/SearchRepository.kt`
- `features/search/SearchScreen.kt`
- `features/search/SearchScreenModel.kt`

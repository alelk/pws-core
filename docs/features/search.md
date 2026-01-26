# Song Search

## Description

Full-text search across songbook songs.

## Search Types

### Global Search (`/v1/songs/search`)
- Searches only in global songs catalog
- Available without authentication
- Used for public song discovery

### User Search (`/v1/user/songs/search`)
- Searches both global and user's songs (merged)
- Requires authentication
- Includes user's custom songbooks with unified ranking

## Search Options

### By Song Number
- Input: `123` or `GS 45`
- Search by song number field
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
    ): SongSearchResponse
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
    ): List<SongSearchSuggestion>
}
```

## Models

### SearchQuery
```kotlin
data class SearchQuery(
    val query: String,
    val type: SearchType = SearchType.ALL,
    val scope: SearchScope = SearchScope.ALL,
    val limit: Int = 20,
    val offset: Int = 0,
    val highlight: Boolean = true
)

enum class SearchType { ALL, NAME, LYRIC, NUMBER }
enum class SearchScope { ALL, GLOBAL, USER_BOOKS }
```

### SongBookReference
Reference to a song in a specific book. Used in search results to show which books contain the song and what number the song has in each book.

```kotlin
data class SongBookReference(
    val bookId: BookId,
    val displayShortName: NonEmptyString,
    val songNumber: Int
)
```

### SongSearchResult
```kotlin
data class SongSearchResult(
    val song: SongSummary,
    val snippet: String,
    val rank: Float,
    val matchedFields: List<MatchedField>,
    val bookReferences: List<SongBookReference> = emptyList()
)
```

### SongSearchSuggestion
```kotlin
data class SongSearchSuggestion(
    val id: SongId,
    val name: NonEmptyString,
    val bookReferences: List<SongBookReference> = emptyList(),
    val snippet: String? = null
)
```

### SongSearchResponse
```kotlin
data class SongSearchResponse(
    val results: List<SongSearchResult>,
    val totalCount: Long,
    val hasMore: Boolean
)
```

### MatchedField
```kotlin
enum class MatchedField {
    NAME,
    LYRIC
}
```

## API Endpoints

| Endpoint | Description | Auth |
|----------|-------------|------|
| `GET /v1/songs/search` | Search global songs | Optional |
| `GET /v1/songs/search/suggestions` | Suggestions from global songs | Optional |
| `GET /v1/user/songs/search` | Search merged (global + user) | Required |
| `GET /v1/user/songs/search/suggestions` | Suggestions merged | Required |

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

### Remote (Exposed/PostgreSQL)
- Backend uses Exposed queries with PostgreSQL full-text search
- Requires internet

## Debounce

When typing text, 300ms debounce is used for suggestions.
When tapping a suggestion, the selected song opens.
When pressing Enter or search button, song search is performed and results are displayed.

## Related Files

- `domain/song/usecase/SearchSongsUseCase.kt`
- `domain/song/usecase/SearchSongSuggestionsUseCase.kt`
- `domain/song/model/SongBookReference.kt`
- `domain/song/model/SongSearchResult.kt`
- `domain/song/model/SongSearchSuggestion.kt`
- `domain/song/model/SongSearchResponse.kt`
- `domain/song/repository/SongSearchRepository.kt`
- `features/search/SearchScreen.kt`
- `features/search/SearchScreenModel.kt`

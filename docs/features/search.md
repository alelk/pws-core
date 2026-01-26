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

### API Uniformity

Both search APIs (`/v1/songs/search` and `/v1/user/songs/search`) are **fully uniform**:

**Search Parameters (identical):**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `query` | string | required | Search text or song number |
| `type` | enum | `ALL` | `ALL`, `NAME`, `LYRIC` |
| `bookId` | string | null | Filter by book ID |
| `limit` | int | 20 | Max results (1-100) |
| `offset` | int | 0 | Pagination offset |
| `highlight` | bool | true | Enable `<mark>` highlighting |

**Suggestion Parameters (identical):**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `query` | string | required | Search text |
| `bookId` | string | null | Filter by book ID |
| `limit` | int | 10 | Max suggestions (1-50) |

**Response Format (identical):**
Both return `SongSearchResponseDto` / `List<SongSearchSuggestionDto>`.

## UI Flow

### Home Screen - Inline Search
The main search experience is on the Home screen with inline suggestions:

```
┌─────────────────────────────────────────────┐
│              HomeScreen                     │
├─────────────────────────────────────────────┤
│  🎵 Psalmist                                │
│  Find your favorite song                    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  🔍 Find a song...                  │    │  ◀── TextField (auto-focus available)
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ 33  Hello to you our vineyard!      │    │  ◀── Suggestions dropdown
│  │     SB-1 | SB-2 | SB-3              │    │      Song number on the left
│  │     <mark>Hello</mark> to you...    │    │      Books with same number grouped
│  │  ─────────────────────────────────  │    │
│  │ 20  Hello to you fighters           │    │
│  │     SB-1                            │    │
│  │     <mark>Hello</mark> to you...    │    │
│  └─────────────────────────────────────┘    │
│                                             │
│  [By number] [By text]                      │  ◀── Quick action buttons
│                                             │
│  Songbooks                                  │
│  ┌─────┐ ┌─────┐ ┌─────┐                    │
│  │ SB  │ │ GS  │ │ HYM │                    │
│  └─────┘ └─────┘ └─────┘                    │
└─────────────────────────────────────────────┘
```

### Suggestion Display Logic

**Same song number across multiple books:**
```
33  Song Title
    SB-1 | SB-2 | SB-3
    Text with matched highlights
```
When clicked → opens song in first book context (SB-1/33)

**Different song numbers in different books:**
The API returns separate suggestions for each unique number.

**Interaction:**
- Type in search field → suggestions appear in dropdown overlay
- Click suggestion → navigate to song
- Press Enter → navigate to full SearchScreen with results

### Search Screen - Full Results
When pressing Enter or clicking "By text" button:

```
┌─────────────────────────────────────────────┐
│  ←  Search                                  │
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐    │
│  │  🔍 Search songs...                 │    │  ◀── TextField (auto-focused)
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

### Navigation on Click

When user clicks a suggestion or search result:
1. **If song has book references** → Navigate to `SongDetailScreen` with `SongNumberId(bookId, songId)` using the first book from the list
2. **If song has no book references** → Navigate to `SongDetailBySongIdScreen` with just `SongId`

This ensures the song is displayed in the context of its book (showing book name, song number) when available.

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
- `features/search/SearchUiState.kt`

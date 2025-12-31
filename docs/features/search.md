# Поиск песен

## Описание

Полнотекстовый поиск по песням сборников.

## Варианты поиска

### По номеру в сборнике
- Ввод: `123` или `БП 45`
- Поиск по полю `SongNumber.number`
- Точное совпадение номера

### По тексту
- Ввод: произвольный текст
- Поиск по:
  - Названию песни
  - Тексту (lyric)
- Подсветка совпадений в результатах

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

## Модели

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
│  │  🔍 Поиск песен...                  │    │  ◀── TextField
│  └─────────────────────────────────────┘    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Подсказки:                         │    │  ◀── Suggestions
│  │  • Благословен Господь              │    │      (появляются при вводе)
│  │  • БП 123                           │    │
│  └─────────────────────────────────────┘    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Результаты:                        │    │  ◀── Search Results
│  │  ┌───────────────────────────────┐  │    │
│  │  │ БП 45 - Благословен Господь   │  │    │
│  │  │ "...Благословен Господь..."   │  │    │
│  │  └───────────────────────────────┘  │    │
│  │  ┌───────────────────────────────┐  │    │
│  │  │ ПП 12 - Благодать             │  │    │
│  │  │ "...благословенный день..."   │  │    │
│  │  └───────────────────────────────┘  │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

## Реализации поиска

### Local (Room FTS5)
- Используется FTS5 для полнотекстового поиска
- Индексы по title, lyric
- Быстрый поиск без интернета

### Remote (Exposed)
- Backend использует запросы Exposed в PostgreSQL
- Требуется интернет

## Debounce

При вводе текста используется debounce 300ms для подсказок.
При нажатии на подсказку, открывается выбранная песня.
При нажатии Enter или кнопки поиска, происходит поиск песни и выдача результатов.

## Связанные файлы

- `domain/song/usecase/SearchSongsUseCase.kt`
- `domain/song/usecase/SearchSongSuggestionsUseCase.kt`
- `domain/song/model/SongSearchResult.kt`
- `domain/song/repository/SearchRepository.kt`
- `features/search/SearchScreen.kt`
- `features/search/SearchScreenModel.kt`



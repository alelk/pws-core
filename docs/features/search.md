# Song Search

## Purpose

Search songs by text or number and provide quick navigation from suggestions/results.

## Main behavior

- Supports full search (`SearchSongsUseCase`) and lightweight suggestions (`SearchSongSuggestionsUseCase`).
- Suggestions include optional `bookReferences` for book-context navigation.
- UI uses debounce before firing requests (home/search screen models).

## Domain models involved

- `SearchQuery`
- `SongSearchResponse`
- `SongSearchResult`
- `SongSearchSuggestion`
- `SongBookReference`

## Navigation rule

When opening a found song:

1. If `bookReferences` is not empty, navigate using `SongNumberId(bookId, songId)` (book context).
2. Otherwise navigate by `SongId` only.

## Data source behavior

- Local targets can serve search through local storage implementation.
- Remote targets use API search resources.
- Endpoint contracts are defined in `api/contract/src/commonMain/kotlin/song/` and `api/contract/src/commonMain/kotlin/usersong/`.

## UI flow summary

- Home screen: inline suggestions while typing.
- Search screen: query editing + suggestions + submit to results flow.
- Suggestion click: immediate navigation to song.

## Related files

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/SearchSongsUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/SearchSongSuggestionsUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/model/SongSearchSuggestion.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/home/HomeScreenModel.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/search/SearchScreenModel.kt`

Last reviewed: 2026-04-29

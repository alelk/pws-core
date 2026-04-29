# Favorites

## Purpose

Allow user to quickly mark and revisit songs.

Favorites support two subject types:

- `FavoriteSubject.BookedSong(songNumberId)`
- `FavoriteSubject.StandaloneSong(songId)`

## User behavior

- Toggle from song screen.
- View reactive favorites list.
- Remove individual items.
- Optional clear-all action (use case available).

## Core use cases

- `ToggleFavoriteUseCase`
- `AddFavoriteUseCase`
- `RemoveFavoriteUseCase`
- `ObserveFavoritesUseCase`
- `ObserveIsFavoriteUseCase`
- `GetFavoritesUseCase`
- `ClearFavoritesUseCase`

## Data model highlights

- `Favorite`: subject + `addedAt`
- `FavoriteSong`: display projection (song name, optional book context, timestamp)

## Repositories

- `FavoriteReadRepository`
- `FavoriteObserveRepository`
- `FavoriteWriteRepository`

## UI implementation notes

- `FavoritesScreenModel` subscribes to `ObserveFavoritesUseCase` and maps to `FavoritesUiState`.
- Navigation from favorites depends on subject type (book-context vs standalone song).

## Related files

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/favorite/model/FavoriteSubject.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/favorite/model/FavoriteSong.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/favorite/usecase/ObserveFavoritesUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/favorite/usecase/ToggleFavoriteUseCase.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/favorites/FavoritesScreen.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/favorites/FavoritesScreenModel.kt`

Last reviewed: 2026-04-29

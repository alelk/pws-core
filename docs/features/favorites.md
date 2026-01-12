# Favorites

## Description

User can add songs to favorites for quick access.

## Behavior

### Adding to Favorites
- ❤️ button on song screen
- Toggle: tapping again removes from favorites
- Addition timestamp is saved

### Favorites List
- Separate screen with list of favorite songs
- Sorting: newest first by default. But custom sorting is possible (from menu), e.g., by name.
- Actions: view song, remove from favorites

## Platform Differences

| Platform        | Storage           | Synchronization                                                              |
|-----------------|-------------------|------------------------------------------------------------------------------|
| Android/iOS     | Local Room DB     | Yes, only if user is authorized and has internet.                            |
| Web/TG Mini App | Backend API       | Yes (tied to account). Without authorization, favorites feature is unavailable. |

## Use Cases

### AddFavoriteUseCase
```kotlin
class ToggleFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(songNumberId: SongNumberId): Boolean =
        txRunner.inRwTransaction { favoriteRepository.toggle(songNumberId) }
}
```

Song is added to favorites with songbook binding (`SongNumberId`, not just `SongId`)

### ObserveFavoritesUseCase

```kotlin
class ObserveFavoritesUseCase(
    private val favoriteRepository: FavoriteObserveRepository
) {
    operator fun invoke(): Flow<List<FavoriteWithSongInfo>> =
        favoriteRepository.observeAll()
}
```

### ObserveFavoriteUseCase

```kotlin
class ObserveIsFavoriteUseCase(
  private val favoriteRepository: FavoriteObserveRepository
) {
  operator fun invoke(songNumberId: SongNumberId): Flow<Boolean> =
    favoriteRepository.observeIsFavorite(songNumberId)
}
```

## Models

### Favorite
```kotlin
data class Favorite(
    val songNumberId: SongNumberId,
    val addedAt: Instant
)
```

### FavoriteSong
```kotlin
data class FavoriteSong(
    val songNumberId: SongNumberId,
    val songNumber: Int,
    val songName: String,
    val bookDisplayName: String,
    val addedAt: Instant
)
```

## UI Components

### FavoriteButton
```kotlin
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
)
```

### FavoritesList
```kotlin
@Composable
fun FavoritesList(
    favorites: List<FavoriteSong>,
    onSongClick: (Long) -> Unit,
    onRemove: (Long) -> Unit
)
```

## UI Flow

```
┌─────────────────────────────────────────────┐
│            FavoritesScreen                  │
├─────────────────────────────────────────────┤
│  ← Favorites                                │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │ GS 45 - Blessed Be the Lord           │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ SV 12 - Grace                         │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ HP 7 - Great God                      │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

- Deletion is done via context menu or swipe left.

## Related Files

- `domain/favorite/model/Favorite.kt`
- `domain/favorite/repository/FavoriteReadRepository.kt`
- `domain/favorite/repository/FavoriteObserveRepository.kt`
- `domain/favorite/repository/FavoriteWriteRepository.kt`
- `domain/favorite/usecase/*.kt`
- `features/favorites/FavoritesScreen.kt`
- `features/favorites/FavoritesScreenModel.kt`
- `features/favorites/FavoritesUiState.kt`

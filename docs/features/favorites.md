# Favorites

## Description

User can add songs to favorites for quick access. Supports two types of favorites:
- **Booked songs**: Songs favorited in context of a specific book (with book ID and song number)
- **Standalone songs**: Songs favorited without book context (only song ID)

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

### ToggleFavoriteUseCase
```kotlin
class ToggleFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(subject: FavoriteSubject): ToggleResourceResult<FavoriteSubject> =
        txRunner.inRwTransaction { favoriteRepository.toggle(subject) }
}
```

### AddFavoriteUseCase
```kotlin
class AddFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(subject: FavoriteSubject): CreateResourceResult<Favorite> =
        txRunner.inRwTransaction { favoriteRepository.add(subject) }
}
```

### RemoveFavoriteUseCase
```kotlin
class RemoveFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(subject: FavoriteSubject): DeleteResourceResult<FavoriteSubject> =
        txRunner.inRwTransaction { favoriteRepository.remove(subject) }
}
```

### GetFavoritesUseCase (for API/backend)
```kotlin
class GetFavoritesUseCase(
    private val favoriteRepository: FavoriteReadRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(limit: Int? = null, offset: Int = 0): List<FavoriteSong> =
        txRunner.inRoTransaction { favoriteRepository.getAll(limit, offset) }
}
```

### ObserveFavoritesUseCase (for UI, reactive)
```kotlin
class ObserveFavoritesUseCase(
    private val favoriteRepository: FavoriteObserveRepository
) {
    operator fun invoke(limit: Int? = null, offset: Int = 0): Flow<List<FavoriteSong>> =
        favoriteRepository.observeAll(limit, offset)
}
```

### ObserveIsFavoriteUseCase
```kotlin
class ObserveIsFavoriteUseCase(
    private val favoriteRepository: FavoriteObserveRepository
) {
    operator fun invoke(subject: FavoriteSubject): Flow<Boolean> =
        favoriteRepository.observeIsFavorite(subject)
}
```

### ClearFavoritesUseCase
```kotlin
class ClearFavoritesUseCase(
    private val favoriteRepository: FavoriteWriteRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(): ClearResourcesResult =
        txRunner.inRwTransaction { favoriteRepository.clearAll() }
}
```

## Models

### FavoriteSubject
```kotlin
sealed interface FavoriteSubject {
    val songId: SongId

    data class BookedSong(val songNumberId: SongNumberId) : FavoriteSubject {
        override val songId: SongId get() = songNumberId.songId
    }

    data class StandaloneSong(override val songId: SongId) : FavoriteSubject
}
```

### Favorite
```kotlin
data class Favorite(
    val subject: FavoriteSubject,
    val addedAt: Instant
)
```

### FavoriteSong
```kotlin
data class FavoriteSong(
    val subject: FavoriteSubject,
    val songName: String,
    val songNumber: Int?,           // null for standalone songs
    val bookDisplayName: String?,   // null for standalone songs
    val addedAt: Instant
)
```

## Repositories

### FavoriteReadRepository (domain)
```kotlin
interface FavoriteReadRepository {
    suspend fun getAll(limit: Int? = null, offset: Int = 0): List<FavoriteSong>
    suspend fun isFavorite(subject: FavoriteSubject): Boolean
    suspend fun count(): Long
}
```

### FavoriteWriteRepository (domain)
```kotlin
interface FavoriteWriteRepository {
    suspend fun add(subject: FavoriteSubject): CreateResourceResult<Favorite>
    suspend fun remove(subject: FavoriteSubject): DeleteResourceResult<FavoriteSubject>
    suspend fun toggle(subject: FavoriteSubject): ToggleResourceResult<FavoriteSubject>
    suspend fun clearAll(): ClearResourcesResult
}
```

### FavoriteObserveRepository (domain)
```kotlin
interface FavoriteObserveRepository {
    fun observeAll(limit: Int? = null, offset: Int = 0): Flow<List<FavoriteSong>>
    fun observeIsFavorite(subject: FavoriteSubject): Flow<Boolean>
}
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

# Избранное

## Описание

Пользователь может добавлять песни в избранное для быстрого доступа.

## Поведение

### Добавление в избранное
- Кнопка ❤️ на экране песни
- Toggle: повторное нажатие убирает из избранного
- Сохраняется timestamp добавления

### Список избранного
- Отдельный экран со списком избранных песен
- Сортировка: по умолчанию новые сверху. Но возможна пользовательская сортировка (из меню), например по имени.
- Действия: просмотр песни, удаление из избранного

## Платформенные различия

| Платформа       | Хранение          | Синхронизация                                                                |
|-----------------|-------------------|------------------------------------------------------------------------------|
| Android/iOS     | Локальная Room DB | Да, только если пользователь авторизован и есть интернет.                    |
| Web/TG Mini App | Backend API       | Да (привязано к аккаунту). Без авторизации функционал избранного недоступен. |

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

В избранное добавляется песня с привязкой к сборнику (`SongNumberId`, а не просто `SongId`)

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

## Модели

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
│  ← Избранное                                │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │ БП 45 - Благословен Господь           │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ПП 12 - Благодать                     │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ИП 7 - Великий Бог                    │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

- Удаление происходит из контекстного меню или смахиванием влево.

## Связанные файлы

- `domain/favorite/model/Favorite.kt`
- `domain/favorite/repository/FavoriteReadRepository.kt`
- `domain/favorite/repository/FavoriteObserveRepository.kt`
- `domain/favorite/repository/FavoriteWriteRepository.kt`
- `domain/favorite/usecase/*.kt`
- `features/favorites/FavoritesScreen.kt`
- `features/favorites/FavoritesScreenModel.kt`
- `features/favorites/FavoritesUiState.kt`


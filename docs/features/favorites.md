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
- Сортировка: новые сверху
- Действия: просмотр песни, удаление из избранного

## Платформенные различия

| Платформа | Хранение | Синхронизация |
|-----------|----------|---------------|
| Android/iOS | Локальная Room DB | Нет |
| Web/TG Mini App | Backend API | Да (привязано к аккаунту) |

## Use Cases

### AddFavoriteUseCase
```kotlin
class AddFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository
) {
    suspend operator fun invoke(songId: Long)
}
```

### RemoveFavoriteUseCase
```kotlin
class RemoveFavoriteUseCase(
    private val favoriteRepository: FavoriteWriteRepository
) {
    suspend operator fun invoke(songId: Long)
}
```

### GetFavoritesUseCase
```kotlin
class GetFavoritesUseCase(
    private val favoriteRepository: FavoriteReadRepository
) {
    operator fun invoke(): Flow<List<FavoriteSong>>
}
```

### CheckFavoriteUseCase
```kotlin
class CheckFavoriteUseCase(
    private val favoriteRepository: FavoriteReadRepository
) {
    operator fun invoke(songId: Long): Flow<Boolean>
}
```

## Модели

### Favorite
```kotlin
data class Favorite(
    val id: Long,
    val songId: Long,
    val addedAt: Instant
)
```

### FavoriteSong
```kotlin
data class FavoriteSong(
    val favorite: Favorite,
    val song: SongSummary  // краткая информация о песне
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
│            FavoritesScreen                   │
├─────────────────────────────────────────────┤
│  ← Избранное                                │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │ БП 45 - Благословен Господь       🗑️ │  │
│  │ Добавлено: 30.12.2024                 │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ПП 12 - Благодать                  🗑️ │  │
│  │ Добавлено: 29.12.2024                 │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ИП 7 - Великий Бог                 🗑️ │  │
│  │ Добавлено: 28.12.2024                 │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## Интеграция с SongScreen

```kotlin
// В SongViewModel
val isFavorite: StateFlow<Boolean> = checkFavoriteUseCase(songId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

fun toggleFavorite() {
    viewModelScope.launch {
        if (isFavorite.value) {
            removeFavoriteUseCase(songId)
        } else {
            addFavoriteUseCase(songId)
        }
    }
}
```

## Связанные файлы

- `domain/favorite/model/Favorite.kt`
- `domain/favorite/repository/FavoriteReadRepository.kt`
- `domain/favorite/repository/FavoriteWriteRepository.kt`
- `domain/favorite/usecase/*.kt`
- `features/favorites/FavoritesScreen.kt`
- `features/favorites/FavoritesViewModel.kt`


# Пользовательские переопределения (User Overrides)

## Концепция

Глобальные песни **неизменяемы** — они управляются администраторами.

Пользователь может создать **override (переопределение)** — свою версию песни, которая:
- Хранится отдельно от глобальной песни
- При отображении **merge'ится** с глобальной
- Видна только создателю

## Что можно переопределить

| Поле       | Описание         |
|------------|------------------|
| `lyric`    | Текст песни      |
| `tonality` | Тональность      |
| `bibleRef` | Ссылка на Библию |
| Примечания | Личные заметки   |

## Merge стратегия

```
Отображаемая песня = Global Song + User Override

Приоритет:
1. Если поле есть в override → берем из override
2. Если поля нет в override → берем из global
```

### Пример

```kotlin
// Global Song
Song(
    id = 1,
    title = "Благословен Господь",
    lyric = "Благословен Господь...",
    tonality = "C"
)

// User Override
UserSongOverride(
    songId = 1,
    lyric = "Благословен Господь!\n...",  // исправленная версия
    tonality = null  // не переопределено
)

// Merged Result
MergedSong(
    id = 1,
    title = "Благословен Господь",           // из global
    lyric = "Благословен Господь!\n...",     // из override
    tonality = "C",                          // из global
    hasUserOverride = true
)
```

## Use Cases

### GetMergedSongDetailUseCase
```kotlin
class GetMergedSongDetailUseCase(
    private val songRepository: SongReadRepository,
    private val overrideRepository: UserOverrideReadRepository
) {
    suspend operator fun invoke(songId: Long): MergedSongDetail? {
        val song = songRepository.getSong(songId) ?: return null
        val override = overrideRepository.getOverride(songId)
        return merge(song, override)
    }
}
```

### CreateUserSongOverrideUseCase
```kotlin
class CreateUserSongOverrideUseCase(
    private val overrideRepository: UserOverrideWriteRepository
) {
    suspend operator fun invoke(command: CreateOverrideCommand): UserSongOverride
}
```

### UpdateUserSongOverrideUseCase
```kotlin
class UpdateUserSongOverrideUseCase(
    private val overrideRepository: UserOverrideWriteRepository
) {
    suspend operator fun invoke(command: UpdateOverrideCommand)
}
```

### DeleteUserSongOverrideUseCase
```kotlin
class DeleteUserSongOverrideUseCase(
    private val overrideRepository: UserOverrideWriteRepository
) {
    suspend operator fun invoke(songId: Long)
}
```

## Модели

### UserSongOverride
```kotlin
data class UserSongOverride(
    val id: Long,
    val songId: Long,
    val lyric: String?,        // null = не переопределено
    val tonality: String?,
    val bibleRef: String?,
    val notes: String?,        // личные заметки
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### MergedSongDetail
```kotlin
data class MergedSongDetail(
    val song: SongDetail,
    val override: UserSongOverride?,
    val hasUserOverride: Boolean
) {
    // Computed properties
    val effectiveLyric: String
        get() = override?.lyric ?: song.lyric
    
    val effectiveTonality: String?
        get() = override?.tonality ?: song.tonality
}
```

### Commands
```kotlin
data class CreateOverrideCommand(
    val songId: Long,
    val lyric: String? = null,
    val tonality: String? = null,
    val bibleRef: String? = null,
    val notes: String? = null
)

data class UpdateOverrideCommand(
    val overrideId: Long,
    val lyric: String? = null,
    val tonality: String? = null,
    val bibleRef: String? = null,
    val notes: String? = null
)
```

## UI Flow

### Индикатор override на экране песни

```
┌─────────────────────────────────────────────┐
│              SongScreen                      │
├─────────────────────────────────────────────┤
│  ← БП 45                    ❤️  ✏️  ⋮       │
├─────────────────────────────────────────────┤
│  Благословен Господь                        │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ ⚠️ Вы редактировали эту песню        │  │  ◀── если есть override
│  │    Показать оригинал                  │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Благословен Господь!                       │
│  Благословенно имя Его!                     │
│  ...                                        │
│                                             │
│  Тональность: C                             │
└─────────────────────────────────────────────┘
```

### Экран редактирования

```
┌─────────────────────────────────────────────┐
│            EditSongScreen                    │
├─────────────────────────────────────────────┤
│  ← Редактирование              [ Сохранить ]│
├─────────────────────────────────────────────┤
│  Текст песни                                │
│  ┌───────────────────────────────────────┐  │
│  │ Благословен Господь!                  │  │
│  │ Благословенно имя Его!                │  │
│  │ ...                                   │  │
│  │                                       │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Тональность                                │
│  ┌───────────────────────────────────────┐  │
│  │ C                                   ▼ │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Заметки (личные)                           │
│  ┌───────────────────────────────────────┐  │
│  │ Петь медленнее во 2-м куплете        │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ 🗑️ Сбросить мои изменения            │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## Будущее: публикация изменений

В будущем пользователь сможет **предложить** свои изменения:

```kotlin
class RequestPublishOverrideUseCase(
    private val overrideRepository: UserOverrideWriteRepository
) {
    suspend operator fun invoke(overrideId: Long)
}
```

Workflow:
1. Пользователь редактирует песню (создает override)
2. Нажимает "Предложить изменения"
3. Создается запрос на модерацию
4. Модератор проверяет и применяет к глобальной песне

## Связанные файлы

- `domain/song/model/UserSongOverride.kt`
- `domain/song/model/MergedSongDetail.kt`
- `domain/song/repository/UserOverrideRepository.kt`
- `domain/song/usecase/GetMergedSongDetailUseCase.kt`
- `domain/song/usecase/CreateUserSongOverrideUseCase.kt`
- `features/song/EditSongScreen.kt`
- `features/song/SongEditViewModel.kt`


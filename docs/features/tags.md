# Теги и категории

## Описание

Теги позволяют категоризировать песни для удобного поиска и навигации.

## Типы тегов

### Глобальные теги
- Определены на backend
- Только для чтения
- Примеры: "Рождество", "Пасха", "Прославление", "Молитва"

### Пользовательские теги
- Создаются пользователем
- Полный CRUD
- Видны только создателю
- Примеры: "Для служения", "Любимые", "Выучить"

## Use Cases

### Чтение тегов

```kotlin
class GetTagsUseCase(
    private val tagRepository: TagReadRepository
) {
    operator fun invoke(): Flow<List<Tag>>
}

class GetSongTagsUseCase(
    private val songTagRepository: SongTagReadRepository
) {
    operator fun invoke(songId: Long): Flow<List<Tag>>
}

class GetSongsByTagUseCase(
    private val songTagRepository: SongTagReadRepository
) {
    operator fun invoke(tagId: Long): Flow<List<SongSummary>>
}
```

### Управление пользовательскими тегами

```kotlin
class CreateTagUseCase(
    private val tagRepository: TagWriteRepository
) {
    suspend operator fun invoke(name: String, color: String?): Tag
}

class UpdateTagUseCase(
    private val tagRepository: TagWriteRepository
) {
    suspend operator fun invoke(tagId: Long, name: String, color: String?)
}

class DeleteTagUseCase(
    private val tagRepository: TagWriteRepository
) {
    suspend operator fun invoke(tagId: Long)
}
```

### Назначение тегов песням

```kotlin
class AddSongTagUseCase(
    private val songTagRepository: SongTagWriteRepository
) {
    suspend operator fun invoke(songId: Long, tagId: Long)
}

class RemoveSongTagUseCase(
    private val songTagRepository: SongTagWriteRepository
) {
    suspend operator fun invoke(songId: Long, tagId: Long)
}
```

## Модели

### Tag
```kotlin
data class Tag(
    val id: Long,
    val name: String,
    val color: String?,      // HEX цвет, например "#FF5722"
    val isGlobal: Boolean,   // true = глобальный, false = пользовательский
    val songCount: Int       // количество песен с этим тегом
)
```

### SongTag
```kotlin
data class SongTag(
    val songId: Long,
    val tagId: Long,
    val assignedAt: Instant
)
```

## UI Components

### TagChip
```kotlin
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = tag.color?.let { Color(it.toColorInt()) } 
            ?: MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = tag.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
```

### TagsRow (на экране песни)
```kotlin
@Composable
fun TagsRow(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            TagChip(tag = tag, onClick = { onTagClick(tag) })
        }
    }
}
```

## UI Flows

### Список тегов

```
┌─────────────────────────────────────────────┐
│              TagsScreen                      │
├─────────────────────────────────────────────┤
│  ← Категории                          ➕    │
├─────────────────────────────────────────────┤
│  Глобальные                                 │
│  ┌─────────┐ ┌─────────┐ ┌───────────────┐  │
│  │Рождество│ │ Пасха   │ │ Прославление  │  │
│  │   45    │ │   32    │ │     128       │  │
│  └─────────┘ └─────────┘ └───────────────┘  │
│                                             │
│  Мои теги                                   │
│  ┌───────────────┐ ┌─────────────────────┐  │
│  │ Для служения  │ │ Выучить          🗑️ │  │
│  │      12       │ │      5               │  │
│  └───────────────┘ └─────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Песни по тегу

```
┌─────────────────────────────────────────────┐
│          TagSongsScreen                      │
├─────────────────────────────────────────────┤
│  ← Рождество (45 песен)                     │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │ БП 45 - Рождественская звезда         │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ПП 12 - В эту ночь                    │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ ИП 7 - Святая ночь                    │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Добавление тега к песне

```
┌─────────────────────────────────────────────┐
│        AddTagBottomSheet                     │
├─────────────────────────────────────────────┤
│  Добавить тег к песне                       │
│                                             │
│  Глобальные                                 │
│  ☐ Рождество                                │
│  ☑ Пасха                                    │
│  ☐ Прославление                             │
│                                             │
│  Мои теги                                   │
│  ☑ Для служения                             │
│  ☐ Выучить                                  │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  ➕ Создать новый тег               │    │
│  └─────────────────────────────────────┘    │
│                                             │
│         [ Готово ]                          │
└─────────────────────────────────────────────┘
```

## Интеграция с SongScreen

```kotlin
// SongScreen показывает теги песни
@Composable
fun SongScreen(songId: Long) {
    val viewModel = koinViewModel<SongViewModel>()
    val tags by viewModel.songTags.collectAsState()
    
    Column {
        // ... текст песни
        
        if (tags.isNotEmpty()) {
            TagsRow(
                tags = tags,
                onTagClick = { tag ->
                    navigator.push(TagSongsScreen(tag.id))
                }
            )
        }
    }
}
```

## Связанные файлы

- `domain/tag/Tag.kt`
- `domain/tag/model/*.kt`
- `domain/tag/repository/*.kt`
- `domain/tag/usecase/*.kt`
- `domain/songtag/repository/*.kt`
- `domain/songtag/usecase/*.kt`
- `features/tags/TagsScreen.kt`
- `features/tags/TagSongsScreen.kt`


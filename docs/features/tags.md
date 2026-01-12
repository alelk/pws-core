# Tags and Categories

## Description

Tags allow categorizing songs for easy search and navigation.

## Tag Types

### Global Tags
- Defined on the backend
- Read-only (but users can define overrides for global tags — change tag color or hide a tag)
- Examples: "Christmas", "Easter", "Worship", "Prayer"

### User Tags
- Created by the user
- Full CRUD support
- Visible only to the creator
- Examples: "For service", "Favorites", "To learn"

## Use Cases

### Reading Tags

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

### Managing User Tags

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

### Assigning Tags to Songs

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

## Models

### Tag
```kotlin
data class Tag(
    val id: Long,
    val name: String,
    val color: String?,      // HEX color, e.g. "#FF5722"
    val isGlobal: Boolean,   // true = global, false = user tag
    val songCount: Int       // number of songs with this tag
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

### TagsRow (on Song Screen)
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

### Tags List

```
┌─────────────────────────────────────────────┐
│              TagsScreen                      │
├─────────────────────────────────────────────┤
│  ← Categories                         ➕    │
├─────────────────────────────────────────────┤
│  Global                                     │
│  ┌─────────┐ ┌─────────┐ ┌───────────────┐  │
│  │Christmas│ │ Easter  │ │   Worship     │  │
│  │   45    │ │   32    │ │     128       │  │
│  └─────────┘ └─────────┘ └───────────────┘  │
│                                             │
│  My Tags                                    │
│  ┌───────────────┐ ┌─────────────────────┐  │
│  │ For service   │ │ To learn         🗑️ │  │
│  │      12       │ │      5               │  │
│  └───────────────┘ └─────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Songs by Tag

```
┌─────────────────────────────────────────────┐
│          TagSongsScreen                      │
├─────────────────────────────────────────────┤
│  ← Christmas (45 songs)                     │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │ SB 45 - Christmas Star                │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ PB 12 - In This Night                 │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │ HB 7 - Holy Night                     │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Adding Tag to Song

```
┌─────────────────────────────────────────────┐
│        AddTagBottomSheet                     │
├─────────────────────────────────────────────┤
│  Add tag to song                            │
│                                             │
│  Global                                     │
│  ☐ Christmas                                │
│  ☑ Easter                                   │
│  ☐ Worship                                  │
│                                             │
│  My Tags                                    │
│  ☑ For service                              │
│  ☐ To learn                                 │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  ➕ Create new tag                  │    │
│  └─────────────────────────────────────┘    │
│                                             │
│         [ Done ]                            │
└─────────────────────────────────────────────┘
```

## Integration with SongScreen

```kotlin
// SongScreen displays song tags
@Composable
fun SongScreen(songId: Long) {
    val viewModel = koinViewModel<SongViewModel>()
    val tags by viewModel.songTags.collectAsState()
    
    Column {
        // ... song lyrics
        
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

## Related Files

- `domain/tag/Tag.kt`
- `domain/tag/model/*.kt`
- `domain/tag/repository/*.kt`
- `domain/tag/usecase/*.kt`
- `domain/songtag/repository/*.kt`
- `domain/songtag/usecase/*.kt`
- `features/tags/TagsScreen.kt`
- `features/tags/TagSongsScreen.kt`

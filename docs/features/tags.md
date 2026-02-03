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
class GetTagsUseCase<ID : TagId>(
    private val tagRepository: TagReadRepository<ID>
) {
    suspend operator fun invoke(sort: TagSort): List<Tag<ID>>
}

class GetTagDetailUseCase<ID : TagId>(
    private val tagRepository: TagReadRepository<ID>
) {
    suspend operator fun invoke(tagId: ID): TagDetail<ID>?
}

class GetSongTagsUseCase<ID : TagId>(
    private val songTagRepository: SongTagReadRepository<ID>
) {
    suspend operator fun invoke(songId: SongId): List<Tag<ID>>
}

class GetTagSongsUseCase<ID : TagId>(
    private val songTagRepository: SongTagReadRepository<ID>
) {
    suspend operator fun invoke(tagId: ID): List<SongWithBookInfo>
}
```

### Managing Tags

```kotlin
class CreateTagUseCase<ID : TagId>(
    private val tagRepository: TagWriteRepository<ID>
) {
    suspend operator fun invoke(command: CreateTagCommand<ID>): CreateResourceResult<ID>
}

class UpdateTagUseCase<ID : TagId>(
    private val tagRepository: TagWriteRepository<ID>
) {
    suspend operator fun invoke(command: UpdateTagCommand<ID>): UpdateResourceResult<ID>
}

class DeleteTagUseCase<ID : TagId>(
    private val tagRepository: TagWriteRepository<ID>
) {
    suspend operator fun invoke(tagId: ID): DeleteResourceResult<ID>
}
```

### User Tags (Server-side)

```kotlin
// Creates custom tag or returns TagDetail
class CreateUserTagUseCase(
    private val userTagWriteRepository: UserTagWriteRepository,
    private val userTagReadRepository: UserTagReadRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        command: CreateTagCommand<TagId>
    ): CreateResourceResult<TagDetail<TagId>>
}

// Updates custom tag or creates override for predefined tag
class UpdateUserTagUseCase(
    private val userTagWriteRepository: UserTagWriteRepository,
    private val userTagReadRepository: UserTagReadRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        command: UpdateTagCommand<TagId>
    ): UpdateResourceResult<TagDetail<TagId>>
}

// Deletes custom tag or hides predefined tag
class DeleteUserTagUseCase(
    private val userTagRepository: UserTagWriteRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        tagId: TagId
    ): DeleteResourceResult<TagId>
}
```

### Assigning Tags to Songs

```kotlin
class AddSongTagUseCase<ID : TagId>(
    private val songTagRepository: SongTagWriteRepository<ID>
) {
    suspend operator fun invoke(songId: SongId, tagId: ID): CreateResourceResult<SongTagAssociation<ID>>
}

class RemoveSongTagUseCase<ID : TagId>(
    private val songTagRepository: SongTagWriteRepository<ID>
) {
    suspend operator fun invoke(songId: SongId, tagId: ID): DeleteResourceResult<SongTagAssociation<ID>>
}

class ReplaceAllSongTagsUseCase<ID : TagId>(
    private val readRepository: SongTagReadRepository<ID>,
    private val writeRepository: SongTagWriteRepository<ID>
) {
    suspend operator fun invoke(songId: SongId, tagIds: Set<ID>): ReplaceAllResourcesResult<SongTagAssociation<ID>>
}
```

## Models

### TagId
```kotlin
sealed interface TagId {
  /** Predefined (global) tag identifier */
  data class Predefined(val identifier: String) : TagId
  
  /** User-created custom tag identifier */
  data class Custom(val id: Long) : TagId
}
```

### Tag
```kotlin
sealed class Tag<out ID : TagId>(
  open val id: ID,
  open val name: String,
  open val priority: Int,
  open val color: Color
) {
  /** Predefined (global) tag, optionally edited by user */
  data class Predefined(
    override val id: TagId.Predefined,
    override val name: String,
    override val priority: Int,
    override val color: Color,
    val edited: Boolean = false  // true if user has overridden properties
  ) : Tag<TagId.Predefined>(id, name, priority, color)

  /** User-created custom tag */
  data class Custom(
    override val id: TagId.Custom,
    override val name: String,
    override val priority: Int,
    override val color: Color
  ) : Tag<TagId.Custom>(id, name, priority, color)
}
```

### TagDetail
```kotlin
sealed class TagDetail<out ID : TagId>(
  open val id: ID,
  open val name: String,
  open val priority: Int,
  open val color: Color,
  open val songCount: Int
) {
  data class Predefined(..., val edited: Boolean) : TagDetail<TagId.Predefined>(...)
  data class Custom(...) : TagDetail<TagId.Custom>(...)
}
```

### SongTagAssociation
```kotlin
data class SongTagAssociation<out ID : TagId>(
  val songId: SongId,
  val tagId: ID
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

### Domain (pws-core)
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/core/ids/TagId.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/model/Tag.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/model/TagDetail.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/repository/*.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/usecase/*.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songtag/repository/*.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songtag/usecase/*.kt`

### API Contract (pws-core)
- `api/contract/src/commonMain/kotlin/tag/TagSummaryDto.kt`
- `api/contract/src/commonMain/kotlin/tag/TagDetailDto.kt`
- `api/contract/src/commonMain/kotlin/core/ids/TagIdDto.kt`

### Server (pws-server)
- `infra/src/main/kotlin/db/table/TagTable.kt`
- `infra/src/main/kotlin/db/table/UserTagTable.kt`
- `infra/src/main/kotlin/db/table/UserTagOverrideTable.kt`
- `infra/src/main/kotlin/repository/tag/*.kt`
- `infra/src/main/kotlin/usecase/tag/*.kt`
- `transport/src/main/kotlin/routes/tagRoutes.kt`
- `transport/src/main/kotlin/routes/userTagRoutes.kt`
- `transport/src/main/kotlin/routes/adminTagRoutes.kt`

### UI (pws-core)
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/tags/TagsScreen.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/tags/TagsScreenModel.kt`

# User Overrides

## Concept

Global songs are **immutable** — they are managed by administrators.

A user can create an **override** — their own version of a song, which:
- Is stored separately from the global song
- Is **merged** with the global song when displayed
- Is visible only to the creator

## API

### Hybrid Approach

The API encapsulates override logic on the backend:

```
# Read — always merged
GET /v1/user/songs/{id}
→ returns UserSongDetail with hasOverride and overriddenFields flags

# Edit — universal
PATCH /v1/user/songs/{id}
→ if global song → creates/updates override
→ if user song → simply updates

# Reset override
DELETE /v1/user/songs/{id}/override
→ removes override, restores global version

# Get original
GET /v1/songs/{id}
→ global song without merge
```

### Merged Response

```json
{
  "id": 123,
  "version": "1.0",
  "locale": "en",
  "name": "Blessed Be the Lord",
  "lyric": { ... },
  "source": "GLOBAL",
  "hasOverride": true,
  "overriddenFields": ["lyric", "bibleRef"]
}
```

## What Can Be Overridden

| Field      | Description      |
|------------|------------------|
| `lyric`    | Song lyrics      |
| `tonality` | Key/Tonality     |
| `bibleRef` | Bible reference  |
| Notes      | Personal notes   |

## Merge Strategy

```
Displayed song = Global Song + User Override

Priority:
1. If field exists in override → take from override
2. If field doesn't exist in override → take from global
```

### Example

```kotlin
// Global Song
Song(
    id = 1,
    title = "Blessed Be the Lord",
    lyric = "Blessed be the Lord...",
    tonality = "C"
)

// User Override
UserSongOverride(
    songId = 1,
    lyric = "Blessed be the Lord!\n...",  // corrected version
    tonality = null  // not overridden
)

// Merged Result
MergedSong(
    id = 1,
    title = "Blessed Be the Lord",         // from global
    lyric = "Blessed be the Lord!\n...",   // from override
    tonality = "C",                        // from global
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

## Models

### UserSongOverride
```kotlin
data class UserSongOverride(
    val id: Long,
    val songId: Long,
    val lyric: String?,        // null = not overridden
    val tonality: String?,
    val bibleRef: String?,
    val notes: String?,        // personal notes
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

### Override Indicator on Song Screen

```
┌─────────────────────────────────────────────┐
│              SongScreen                     │
├─────────────────────────────────────────────┤
│  ← SB 45                    ❤️  ✏️  ⋮       │
├─────────────────────────────────────────────┤
│  Blessed Be the Lord                        │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ ⚠️ You have edited this song          │  │  ◀── if override exists
│  │    Show original                      │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Blessed be the Lord!                       │
│  Blessed be His name!                       │
│  ...                                        │
│                                             │
│  Key: C                                     │
└─────────────────────────────────────────────┘
```

### Edit Screen

```
┌─────────────────────────────────────────────┐
│            EditSongScreen                    │
├─────────────────────────────────────────────┤
│  ← Edit                            [ Save ] │
├─────────────────────────────────────────────┤
│  Song lyrics                                │
│  ┌───────────────────────────────────────┐  │
│  │ Blessed be the Lord!                  │  │
│  │ Blessed be His name!                  │  │
│  │ ...                                   │  │
│  │                                       │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Key                                        │
│  ┌───────────────────────────────────────┐  │
│  │ C                                   ▼ │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Notes (personal)                           │
│  ┌───────────────────────────────────────┐  │
│  │ Sing slower in 2nd verse              │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ 🗑️ Reset my changes                   │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## Future: Publishing Changes

In the future, users will be able to **propose** their changes:

```kotlin
class RequestPublishOverrideUseCase(
    private val overrideRepository: UserOverrideWriteRepository
) {
    suspend operator fun invoke(overrideId: Long)
}
```

Workflow:
1. User edits a song (creates override)
2. Clicks "Propose changes"
3. A moderation request is created
4. Moderator reviews and applies to global song

## Related Files

- `domain/song/model/UserSongOverride.kt`
- `domain/song/model/MergedSongDetail.kt`
- `domain/song/repository/UserOverrideRepository.kt`
- `domain/song/usecase/GetMergedSongDetailUseCase.kt`
- `domain/song/usecase/CreateUserSongOverrideUseCase.kt`
- `features/song/EditSongScreen.kt`
- `features/song/SongEditViewModel.kt`

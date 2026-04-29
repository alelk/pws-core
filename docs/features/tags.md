# Tags and Categories

## Purpose

Tags provide thematic grouping of songs and support navigation to songs-by-tag.

## Tag model

- `TagId.Predefined(identifier)` for global tags.
- `TagId.Custom(identifier)` for user tags.
- `Tag.Predefined` and `Tag.Custom` as domain variants.

## Main behavior

- View tags list (reactive).
- Create/update/delete custom tags.
- Hide/override predefined tags through user operations (implementation-specific per backend/local adapter).
- Open songs for selected tag.
- Replace all tags for a song in one operation.

## Core use cases

- Tag management:
  - `ObserveTagsUseCase`
  - `GetTagsUseCase`
  - `GetTagDetailUseCase`
  - `CreateTagUseCase`
  - `UpdateTagUseCase`
  - `DeleteTagUseCase`
- Song-tag relations:
  - `ObserveTagsForSongUseCase`
  - `GetSongTagsUseCase`
  - `ObserveSongsByTagUseCase`
  - `ReplaceAllSongTagsUseCase`

## UI implementation notes

- `TagsScreenModel` handles list + CRUD interactions and snackbar effects.
- Songs-by-tag screen lives in `features/tags/songs/`.
- Song detail flow uses relation use cases to render chips and persist edits.

## Related files

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/core/ids/TagId.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/model/Tag.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/tag/usecase/ObserveTagsUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/songtag/usecase/ReplaceAllSongTagsUseCase.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/tags/TagsScreenModel.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/tags/songs/TagSongsScreenModel.kt`

Last reviewed: 2026-04-29

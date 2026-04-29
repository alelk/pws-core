# User Overrides

## Purpose

Allow a user to personalize global songs without mutating global canonical data.

## Concept

- Global song remains canonical.
- User-specific override stores changed fields.
- UI receives merged view (`MergedSongDetail`) with metadata:
  - `hasOverride`
  - `overriddenFields`
  - `source`

## Core use cases

- `GetMergedSongDetailUseCase`
- `OverrideSongUseCase`
- `ResetSongOverrideUseCase`
- `GetOverriddenSongIdsUseCase`

## Typical flow

1. Load merged song for `(userId, songId)`.
2. User submits override command with changed fields.
3. Save override and return updated merged view.
4. Optional reset removes override and falls back to global song.

## Fields and merge semantics

- Only specified fields are overridden.
- Unspecified fields remain from global song.
- Overridden field detection is used for UI indicators.

## API contract pointers

- User song contracts: `api/contract/src/commonMain/kotlin/usersong/`
- Base song contracts: `api/contract/src/commonMain/kotlin/song/`

## Related files

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/model/MergedSongDetail.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/command/OverrideSongCommand.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/GetMergedSongDetailUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/OverrideSongUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/song/usecase/ResetSongOverrideUseCase.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/song/edit/SongEditScreenModel.kt`

Last reviewed: 2026-04-29


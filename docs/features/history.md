# View History

## Purpose

Record recently viewed songs and provide quick access back to them.

History supports two subject types:

- `HistorySubject.BookedSong(songNumberId)`
- `HistorySubject.StandaloneSong(songId)`

## Main behavior

- Song view is recorded after a view-time threshold (screen logic).
- Re-opening a song updates existing entry metadata (view count/timestamp) instead of uncontrolled duplication.
- History list is reactive and supports remove/clear actions.

## Core use cases

- `RecordSongViewUseCase`
- `ObserveHistoryUseCase`
- `GetHistoryUseCase`
- `RemoveHistoryEntryUseCase`
- `ClearHistoryUseCase`

## Data model highlights

- `HistoryEntry`: subject + display metadata + `viewedAt` + `viewCount`
- `HistorySubject`: booked vs standalone song identity

## UI implementation notes

- `HistoryScreenModel` subscribes to `ObserveHistoryUseCase`.
- Clear-all confirmation is controlled inside screen model state/effects.
- Song detail flow is responsible for deciding when to call `RecordSongViewUseCase`.

## Related files

- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/history/model/HistoryEntry.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/history/model/HistorySubject.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/history/usecase/RecordSongViewUseCase.kt`
- `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/history/usecase/ObserveHistoryUseCase.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/history/HistoryScreen.kt`
- `features/src/commonMain/kotlin/io/github/alelk/pws/features/history/HistoryScreenModel.kt`

Last reviewed: 2026-04-29

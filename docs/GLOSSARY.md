# Glossary

Terms used in code and docs.
For naming conventions (`{Action}{Entity}UseCase`, etc.) see [`../AGENTS.md`](../AGENTS.md) § 6.

---

## Business terms

| Term                                 | Meaning                                                                                |
|--------------------------------------|----------------------------------------------------------------------------------------|
| `Song`                               | Song aggregate and related song models                                                 |
| `SongDetail`                         | Full song for detail screen                                                            |
| `SongSummary`                        | Compact song for lists / search                                                        |
| `MergedSongDetail`                   | Song with user overrides applied (carries `hasOverride`, `overriddenFields`, `source`) |
| `Book`                               | Songbook                                                                               |
| `SongNumber`                         | Human-visible number inside a book (e.g. `45`)                                         |
| `SongNumberId`                       | Stable identity of a song within a book — `(bookId, songId)`                           |
| `Tag`                                | Category label for songs (`Tag.Predefined` or `Tag.Custom`)                            |
| `Favorite`                           | User favorite marker for a song subject                                                |
| `HistoryEntry`                       | Song view history item with timestamp + view count                                     |
| `SongReference`                      | Relation between similar songs                                                         |
| `FavoriteSubject` / `HistorySubject` | Sealed variants: `BookedSong(songNumberId)` vs `StandaloneSong(songId)`                |

## Technical terms

| Term                | Meaning                                                            |
|---------------------|--------------------------------------------------------------------|
| `UseCase`           | Single-method business operation in domain                         |
| `Repository`        | Domain data-access contract (interface only in `:domain`)          |
| `Command`           | Write-intent object (e.g. `UpdateSongCommand`)                     |
| `Query`             | Read filter/sort object (e.g. `BookQuery(enabled = true)`)         |
| `OptionalField<T>`  | PATCH-style patch semantics: `Unchanged \| Set(value) \| Clear`    |
| `TransactionRunner` | Transaction-boundary abstraction injected into use cases           |
| `Flow`              | Reactive stream for observe paths                                  |
| `StateScreenModel`  | Voyager state holder used by features                              |
| `Screen`            | Voyager navigation entry with composable content                   |
| `UiState`           | Sealed `Loading \| Content \| Error` per screen                    |
| `Effect`            | One-shot UI signal delivered via `Channel<Effect>(BUFFERED)`       |
| `UiMessage`         | Typed user-facing message (see `features/.../app/UiMessage.kt`)    |
| `DTO`               | API transfer model in `:api:contract`                              |

## Key distinctions

### `SongNumber` vs `SongNumberId`

- `SongNumber` — the visible number printed in a book (e.g. `45`).
- `SongNumberId` — the stable identity of a specific song within a specific book (`bookId/songId`). Navigation should prefer this when book context is known.

### Repository suffixes

| Suffix    | Shape                                                                            |
|-----------|----------------------------------------------------------------------------------|
| `Read`    | One-shot fetch APIs (`suspend`)                                                  |
| `Observe` | Reactive APIs returning `Flow<T>`                                                |
| `Write`   | Mutation APIs (create / update / delete / toggle…) returning sealed result types |
| `Search`  | Search-specific queries (e.g. `SongSearchRepository`)                            |

### `Predefined` vs `Custom` tags

- `Tag.Predefined` / `TagId.Predefined(identifier)` — global tags shipped with the catalog.
- `Tag.Custom` / `TagId.Custom(identifier)` — user-created tags.

Last reviewed: 2026-06-17

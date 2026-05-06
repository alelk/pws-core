# Glossary

Key terms used across `pws-core` docs and code.

## Business terms

| Term | Meaning |
|---|---|
| `Song` | Song aggregate and related song models |
| `SongDetail` | Full song for detail screen |
| `SongSummary` | Compact song for lists/search |
| `Book` | Songbook |
| `SongNumber` | Human-visible number in book |
| `SongNumberId` | Unique song-in-book id (`bookId/songId`) |
| `Tag` | Category label for songs |
| `Favorite` | User favorite marker for a song subject |
| `HistoryEntry` | Song view history item |
| `SongReference` | Relation between similar songs |
| `MergedSongDetail` | Song with user overrides applied |

## Technical terms

| Term | Meaning |
|---|---|
| `UseCase` | Business operation class in domain |
| `Repository` | Domain data access contract |
| `Command` | Write intent object |
| `Query` | Read filter/sort object |
| `OptionalField` | Patch semantics: unchanged/set/clear |
| `TransactionRunner` | Transaction boundary abstraction |
| `Flow` | Reactive stream for observe paths |
| `StateScreenModel` | Voyager state holder used by features |
| `Screen` | Voyager navigation entry with composable content |
| `DTO` | API transfer model in `:api:contract` |

## Module terms

| Module | Role |
|---|---|
| `:domain` | Business contracts and logic |
| `:api:contract` | API resource contracts and DTOs |
| `:api:mapping` | DTO-domain adapters |
| `:api:client` | Remote repository implementations |
| `:features` | Screens and screen models |
| `:core:navigation` | Shared navigation contract (`SharedScreens`) |
| `:core:ui` | Shared UI primitives |
| `:data:db-room` | Local DB schema/DAO layer |
| `:data:repo-room` | Local repository implementations |
| `:portable-data` | Portable serialisation formats: `Backup` (user data export/import), `CollectionBundle` (full deduplicated collection for asset delivery), `BookBundle` (single book for dynamic delivery) |

## Naming conventions

| Pattern | Example |
|---|---|
| `{Action}{Entity}UseCase` | `GetSongDetailUseCase` |
| `{Entity}{Read|Write|Observe}Repository` | `SongObserveRepository` |
| `Remote{Entity}{Read|Write}Repository` | `RemoteSongReadRepository` |
| `{Feature}Screen` | `SongDetailScreen` |
| `{Feature}ScreenModel` | `SongDetailScreenModel` |
| `{Feature}UiState` | `SongDetailUiState` |

## Important distinctions

### `SongNumber` vs `SongNumberId`

- `SongNumber`: visible number inside a book (e.g., `45`).
- `SongNumberId`: stable identity for a song within book context (`bookId/songId`).

### Repository suffixes

- `Read`: one-shot fetch APIs (`suspend`).
- `Observe`: reactive APIs (`Flow`).
- `Write`: mutation APIs (create/update/delete/toggle/etc.).

Last reviewed: 2026-04-29

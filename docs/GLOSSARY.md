# Glossary

Key terms and concepts of the PWS Core project.

## Business Terms

| Term              | Description                                                           |
|-------------------|-----------------------------------------------------------------------|
| **Song**          | A song with lyrics, metadata, and tonality                            |
| **SongDetail**    | Full song with all fields (lyric, author, composer, tonalities)       |
| **SongSummary**   | Brief song info for lists (id, name, locale)                          |
| **Book**          | A songbook (e.g., "God's Song", "Songs of Victory")                   |
| **SongNumber**    | Song number in a songbook (one song can be in multiple songbooks)     |
| **SongNumberId**  | Unique identifier: bookId/songId (e.g., "psalms/123")                 |
| **Tag**           | Song category/tag ("Christmas", "Easter", "Worship")                  |
| **Favorite**      | User's favorite song                                                  |
| **HistoryEntry**  | Song view history record                                              |
| **SongReference** | Link between similar songs                                            |
| **Lyric**         | Song text with markup (verses, choruses, bridges)                     |
| **LyricPart**     | Single part of lyric: Verse, Chorus, or Bridge                        |
| **Person**        | Author, translator, or composer of a song                             |
| **Tonality**      | Musical key of a song (e.g., "A major", "C minor")                    |
| **Version**       | Semantic version of song/book data (major.minor)                      |

## Technical Terms

| Term                | Description                                                |
|---------------------|------------------------------------------------------------|
| **Use Case**        | A class encapsulating a business operation                 |
| **Repository**      | Interface for data access (abstraction over DB/API)        |
| **Command**         | Object representing a write operation (CreateSongCommand)  |
| **Query**           | Object representing read parameters (SongQuery)            |
| **Value Object**    | Immutable object defined by its value (SongId, BookId)     |
| **OptionalField**   | Wrapper for patch semantics (Unchanged/Set/Clear)          |
| **TransactionRunner** | Abstraction for DB transactions                          |
| **Sealed Result**   | Type-safe result of operation (Success/Failure)            |
| **DTO**             | Data Transfer Object — serializable model for API          |
| **Domain Model**    | Business model, independent of data source                 |
| **ViewModel**       | Component connecting UI with Use Cases                     |
| **Screen**          | Voyager Screen — navigation screen with Composable         |
| **Flow**            | Kotlin Flow — reactive data stream                         |
| **Arb**             | Kotest arbitrary generator for property-based testing      |

## Modules

| Term                   | Description                                              |
|------------------------|----------------------------------------------------------|
| `:domain`              | Business logic (models, use cases, repository interfaces)|
| `:domain:lyric-format` | Song lyrics parsing and formatting                       |
| `:domain:domain-test-fixtures` | Test data generators (Kotest Arb)                |
| `:api:contract`        | DTOs for HTTP API                                        |
| `:api:client`          | Ktor HTTP client                                         |
| `:api:mapping`         | DTO ↔ Domain mapping                                     |
| `:features`            | UI screens and components                                |
| `:core:navigation`     | Navigation definitions                                   |
| `:core:ui`             | Shared UI utilities                                      |
| `:data:db-room`        | Room database                                            |
| `:data:repo-room`      | Room repositories                                        |
| `:backup`              | Backup and restore                                       |

## Naming Conventions

| Pattern                     | Example                 | Description                     |
|-----------------------------|-------------------------|---------------------------------|
| `{Action}{Entity}UseCase`   | `GetSongDetailUseCase`  | Use Case for an operation       |
| `{Entity}ReadRepository`    | `SongReadRepository`    | Repository for reading          |
| `{Entity}WriteRepository`   | `SongWriteRepository`   | Repository for writing          |
| `{Entity}ObserveRepository` | `SongObserveRepository` | Repository for Flow subscription|
| `Remote{Entity}Repository`  | `RemoteSongRepository`  | Remote implementation           |
| `{Feature}Screen`           | `SongScreen`            | Voyager Screen                  |
| `{Feature}ViewModel`        | `SongViewModel`         | ViewModel                       |
| `{Feature}UiState`          | `SongUiState`           | Sealed class for states         |
| `{Entity}Dto`               | `SongDto`               | API DTO                         |

## Package Names

| Module             | Base Package                          |
|--------------------|---------------------------------------|
| `:domain`          | `io.github.alelk.pws.domain`          |
| `:features`        | `io.github.alelk.pws.features`        |
| `:api:*`           | `io.github.alelk.pws.api`             |
| `:core:navigation` | `io.github.alelk.pws.core.navigation` |
| `:core:ui`         | `io.github.alelk.pws.core.ui`         |

## Important Distinctions

### SongNumber vs SongNumberId

| Type           | Format              | Purpose                                    |
|----------------|---------------------|--------------------------------------------|
| `SongNumber`   | `bookId#number`     | Human-readable song number in book (e.g., "psalms#42") |
| `SongNumberId` | `bookId/songId`     | Unique database ID (e.g., "psalms/12345")  |

`SongNumber` uses the printed number in the songbook (1, 2, 3...).
`SongNumberId` uses the internal `SongId` (database primary key).

### SongDetail vs SongSummary

| Type          | Usage                                     |
|---------------|-------------------------------------------|
| `SongDetail`  | Full song with lyric, author, tonalities — for song screen |
| `SongSummary` | Minimal info (id, name, locale, edited) — for lists |

### Read vs Write vs Observe Repositories

| Suffix    | Returns         | Purpose                      |
|-----------|-----------------|------------------------------|
| `Read`    | `suspend fun`   | One-time data fetch          |
| `Write`   | `suspend fun`   | Create/Update/Delete         |
| `Observe` | `Flow<>`        | Reactive stream of changes   |

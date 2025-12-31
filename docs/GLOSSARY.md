# Glossary

Key terms and concepts of the PWS Core project.

## Business Terms

| Term              | Description                                                           |
|-------------------|-----------------------------------------------------------------------|
| **Song**          | A song with lyrics, metadata, and tonality                            |
| **Book**          | A songbook (e.g., "God's Song", "Songs of Victory")                   |
| **SongNumber**    | Song number in a songbook (one song can be in multiple songbooks)     |
| **Tag**           | Song category/tag ("Christmas", "Easter", "Worship")                  |
| **Favorite**      | User's favorite song                                                  |
| **History**       | Song view history record                                              |
| **SongReference** | Link between similar songs                                            |
| **Lyric**         | Song text with markup (verses, choruses, bridges)                     |

## Technical Terms

| Term             | Description                                            |
|------------------|--------------------------------------------------------|
| **Use Case**     | A class encapsulating a business operation             |
| **Repository**   | Interface for data access (abstraction over DB/API)    |
| **DTO**          | Data Transfer Object — serializable model for API      |
| **Domain Model** | Business model, independent of data source             |
| **ViewModel**    | Component connecting UI with Use Cases                 |
| **Screen**       | Voyager Screen — navigation screen with Composable     |
| **Flow**         | Kotlin Flow — reactive data stream                     |

## Modules

| Term                   | Description                                              |
|------------------------|----------------------------------------------------------|
| `:domain`              | Business logic (models, use cases, repository interfaces)|
| `:domain:lyric-format` | Song lyrics parsing and formatting                       |
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

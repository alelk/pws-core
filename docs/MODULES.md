# PWS Core Modules

This page lists current modules from `settings.gradle.kts` and their responsibilities.

## Module inventory

| Module | Purpose |
|---|---|
| `:domain` | Business models, commands/queries, use cases, repository interfaces |
| `:domain:lyric-format` | Lyrics parser/formatter |
| `:domain:domain-test-fixtures` | Test data generators/helpers |
| `:api:contract` | HTTP DTOs and Ktor `@Resource` contracts |
| `:api:mapping` | DTO <-> domain mapping |
| `:api:client` | Ktor API client + remote repository implementations |
| `:api:client:di` | DI wiring for API client modules |
| `:core:navigation` | Shared navigation contracts (`SharedScreens`) |
| `:core:ui` | Shared UI primitives/utilities |
| `:features` | Compose Multiplatform screens + screen models |
| `:data:db-room` | Room schema, entities, DAOs |
| `:data:db-room:db-room-test-fixtures` | Test helpers/fixtures for Room module |
| `:data:repo-room` | Room-backed repository implementations |
| `:portable-data` | Portable serialization formats: `Backup` (user data export/import), `CollectionBundle` (full deduplicated collection for assets), `BookBundle` (single book for dynamic delivery). Shared sub-models: `Book`, `Song`, `Tag`, `SongReference`. Format: YAML (kaml) + gzip on JVM. |

## Important note about sync

- There is currently no standalone `:sync` module in this repository.
- Synchronization concepts are documented in `docs/SYNC.md` as architecture/roadmap guidance.

## Typical dependency direction

```text
:features -> :domain, :core:navigation, :core:ui

:api:client -> :domain, :api:contract, :api:mapping
:api:mapping -> :domain, :api:contract

:data:repo-room -> :domain, :data:db-room

:portable-data -> :domain
:domain:domain-test-fixtures -> :domain

:portable-data -> :domain
```

## Key code locations

- Domain root: `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/`
- Features root: `features/src/commonMain/kotlin/io/github/alelk/pws/features/`
- API client repos: `api/client/src/commonMain/kotlin/repository/`
- Room repos: `data/repo-room/src/commonMain/kotlin/io/github/alelk/pws/data/repository/room/`
- API contracts: `api/contract/src/commonMain/kotlin/`

## Feature-module conventions

Typical feature package in `:features`:

```text
{feature}/
  {Feature}Screen.kt
  {Feature}ScreenModel.kt
  {Feature}UiState.kt
  {feature}ScreenModelModule.kt
  {feature}ScreenModule.kt
```

Primary presentation pattern is Voyager `Screen` + `StateScreenModel`.

Last reviewed: 2026-05-06

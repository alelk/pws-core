# PWS Core - Context for AI

This document gives fast project context for coding agents.
Operational rules and style are in `docs/ai/CONTRIBUTING.md`.

## Product scope

PWS (Praise & Worship Songs) is a multiplatform Christian songbook.

| Platform | Data source | Offline |
|---|---|---|
| Android/iOS | Local Room DB | Yes |
| Web/Telegram Mini App | Remote API | No |

## Repository ecosystem

| Repository | Role |
|---|---|
| `pws-core` | Shared multiplatform library (this repo) |
| `pws-server` | Backend API and search |
| `pws-android` | Android app consuming `pws-core` |

Critical compatibility rule:

- Keep `:api:contract` and `:api:mapping` aligned with `pws-server` API contracts.

## Architecture at a glance

Main flow:

```text
Screen (Compose + Voyager)
  -> StateScreenModel
    -> UseCase
      -> Repository interface (domain)
        -> Room implementation OR Remote implementation
```

High-level principles:

- Domain-first and platform-agnostic.
- Use cases own transaction boundaries.
- Reactive read paths use `Flow`.
- Mobile behavior is offline-first.

## Technology stack

- Kotlin Multiplatform
- Compose Multiplatform
- Voyager
- Koin
- Ktor
- Room
- kotlinx.serialization
- Kotest

## Modules (source of truth: `settings.gradle.kts`)

```text
:domain
:domain:domain-test-fixtures
:domain:lyric-format

:api:contract
:api:mapping
:api:client
:api:client:di

:core:navigation
:core:ui

:features

:backup

:data:db-room
:data:db-room:db-room-test-fixtures
:data:repo-room
```

## Domain shape

Primary domain areas live under:

`domain/src/commonMain/kotlin/io/github/alelk/pws/domain/`

- `song`, `book`, `songnumber`, `tag`, `songtag`
- `favorite`, `history`, `songreference`
- `auth`, `payment`, `bookstatistic`
- shared primitives under `core`

Typical package layout per entity:

```text
{entity}/
  model/
  repository/
  usecase/
  command/
  query/
```

## Key domain patterns

### Value objects and validation

```kotlin
@JvmInline
value class SongId(val value: Long) {
  init { require(value >= 0) }
}
```

### Patch semantics with `OptionalField`

Use when updates must distinguish unchanged vs set vs clear values.

### Sealed operation results

Write operations return typed results (`CreateResourceResult`, `UpdateResourceResult`, etc.) rather than throwing for expected business outcomes.

### Transaction boundary in use cases

Use `TransactionRunner` in use cases, not in UI code.

## Naming conventions

- Use case: `{Action}{Entity}UseCase` (`GetSongDetailUseCase`, `ObserveBooksUseCase`)
- Repository interface: `{Entity}{Read|Write|Observe}Repository`
- Feature state holder: `{Feature}ScreenModel` (Voyager `StateScreenModel`)
- UI entrypoint: `{Feature}Screen`
- Remote repo impl: `Remote{Entity}{Read|Write}Repository`

## Important behavior notes

- Book visibility is priority-driven (`enabled=true` maps to active priority range).
- Search can include book references for context navigation.
- Mobile sync behavior and conflict rules are documented in `docs/SYNC.md`.

## Where to look next

- Feature-level behavior: `docs/FEATURES.md` and `docs/features/*.md`
- Data contracts and mappings: `docs/DATA_FLOW.md`
- Architecture and dependency layout: `docs/ARCHITECTURE.md`, `docs/MODULES.md`

Last reviewed: 2026-04-29

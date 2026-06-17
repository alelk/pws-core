# Modules

Dependency graph and code locations.
For the module inventory table see [`../AGENTS.md`](../AGENTS.md) § 4.

Source of truth: [`../settings.gradle.kts`](../settings.gradle.kts).

---

## Dependency direction

```text
:features ─► :domain
         ├─► :core:navigation
         └─► :core:ui

:api:client ─► :domain
           ├─► :api:contract
           └─► :api:mapping
:api:mapping ─► :domain
             └─► :api:contract
:api:client:di ─► :api:client

:data:repo-room ─► :domain
                └─► :data:db-room

:portable-data ─► :domain
:domain:lyric-format ─► :domain
:domain:domain-test-fixtures ─► :domain
:data:db-room:db-room-test-fixtures ─► :data:db-room
```

**Forbidden direction:** `:domain` must never depend on `:features`, `:api:*`, `:data:*`, or platform code.

---

## Code locations (navigation map)

| What                            | Path                                                                                                   |
|---------------------------------|--------------------------------------------------------------------------------------------------------|
| Domain entities + use cases     | `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/`                                             |
| Core identifiers (e.g. `TagId`) | `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/core/ids/`                                    |
| Feature screens + ScreenModels  | `features/src/commonMain/kotlin/io/github/alelk/pws/features/`                                         |
| Shared `UiMessage` type         | `features/src/commonMain/kotlin/io/github/alelk/pws/features/app/UiMessage.kt`                         |
| Shared navigation contracts     | `core/navigation/src/commonMain/kotlin/.../SharedScreens.kt`                                           |
| Remote repository impls         | `api/client/src/commonMain/kotlin/repository/`                                                         |
| Room repository impls           | `data/repo-room/src/commonMain/kotlin/io/github/alelk/pws/data/repository/room/`                       |
| API DTO contracts               | `api/contract/src/commonMain/kotlin/{book,song,tag,favorite,history,usersong,usertag,userbook,admin}/` |
| Lyrics parser                   | `domain/lyric-format/src/commonMain/kotlin/io/github/alelk/pws/domain/lyric/format/`                   |
| Portable bundles                | `portable-data/src/commonMain/kotlin/io/github/alelk/pws/portable/`                                    |

---

## Feature package convention

Each feature lives under `features/src/commonMain/kotlin/io/github/alelk/pws/features/{feature}/`:

```text
{feature}/
  {Feature}Screen.kt              Voyager Screen
  {Feature}ScreenModel.kt         StateScreenModel<UiState>
  {Feature}UiState.kt             sealed Loading | Content | Error
  {feature}ScreenModelModule.kt   (optional) ScreenModel Koin module
  {feature}ScreenModule.kt        feature wiring Koin module
```

Current feature directories: `app`, `book`, `books`, `components`, `di`, `favorites`, `history`, `home`, `resources`, `search`, `settings`, `song`, `tags`, `theme`.

---

## Domain entity convention

Each entity lives under `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/`:

```text
{entity}/
  model/         entities, value objects
  repository/    {Entity}{Read|Write|Observe}Repository interfaces
  usecase/       {Action}{Entity}UseCase
  command/       UpdateXxxCommand + OptionalField<...> patches
  query/         filter/sort objects
```

Current domain dirs: `auth`, `book`, `bookstatistic`, `core`, `cross`, `donationprompt`, `favorite`, `history`, `payment`, `person`, `song`, `songnumber`, `songreference`, `songtag`, `tag`, `tonality`.

---

## Related

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — layer responsibilities + dependency rules
- [`../AGENTS.md`](../AGENTS.md) § 4–5 — module purpose table + where code belongs

Last reviewed: 2026-06-17

# AGENTS.md — pws-core

> Canonical operational runbook for **any** AI coding agent (Claude Code, Cursor, Aider, Copilot,
> Codex, Cody…).
> Auto-loaded by tools that follow the AGENTS.md convention. Claude Code reads `CLAUDE.md` first (a
> thin wrapper) and is pointed here.
> Single source of truth — do not duplicate this content elsewhere. Deep references live under
`docs/`.

---

## 1. Repo at a glance

- **Role:** shared **Kotlin Multiplatform** library for the PWS songbook ecosystem — domain ·
  features (Compose MP) · API client · local data · portable serialisation.
- **Consumers:** `pws-android` (Android app), `pws-server` (backend), web / Telegram Mini App (
  planned).
- **Hard contract:** `:api:contract` + `:api:mapping` must stay aligned with `pws-server`.
- **Toolchain:** JDK 21 · Kotlin 2.3.21 · Gradle wrapper · Compose Multiplatform · Voyager · Koin ·
  Ktor · Room · kotlinx.serialization · Kotest.

---

## 2. Hot paths (most common commands)

| Goal                               | Command                                            |
|------------------------------------|----------------------------------------------------|
| Fast compile (touched module)      | `./gradlew :<module>:compileKotlinJvm`             |
| Run module unit tests              | `./gradlew :<module>:jvmTest`                      |
| Full multiplatform assemble        | `./gradlew :features:assemble`                     |
| Common smoke before declaring done | `./gradlew :domain:jvmTest :portable-data:jvmTest` |

**Rule of thumb:** module-scoped tasks first. Only run repo-wide `:assemble` / checks before
declaring work done.

---

## 3. Architecture (one screen)

```text
Screen (Compose + Voyager)
  → StateScreenModel
    → UseCase                      ← transaction boundary lives here
      → Repository interface (domain)
        → Room impl (local)  OR  Ktor impl (remote)
```

- **Domain-first**, platform-agnostic (`commonMain`, no `java.*` / Android / iOS APIs).
- **DI** (Koin) chooses local vs. remote per host app.
- **Read** paths reactive via `Flow` (`Observe*UseCase`).
- **Write** paths return typed sealed results, not exceptions for expected outcomes.
- **Offline-first** on mobile; web/Telegram Mini App are remote-only.

Deep dive: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), [`docs/MODULES.md`](docs/MODULES.md), [
`docs/DATA_FLOW.md`](docs/DATA_FLOW.md).

---

## 4. Module map (canonical: `settings.gradle.kts`)

| Module                               | Purpose                                                                                                  |
|--------------------------------------|----------------------------------------------------------------------------------------------------------|
| `:domain`                            | Models, commands/queries, use cases, repository interfaces                                               |
| `:domain:lyric-format`               | Lyrics parser/formatter                                                                                  |
| `:domain:domain-test-fixtures`       | Test data generators                                                                                     |
| `:api:contract`                      | HTTP DTOs + Ktor `@Resource` definitions                                                                 |
| `:api:mapping`                       | DTO ↔ domain adapters                                                                                    |
| `:api:client` (+ `:di`)              | Ktor client + remote repository implementations                                                          |
| `:core:navigation`                   | Shared navigation contracts (`SharedScreens`)                                                            |
| `:core:ui`                           | Shared UI primitives                                                                                     |
| `:features`                          | Compose MP screens + screen models                                                                       |
| `:data:db-room` (+ `-test-fixtures`) | Room schema, entities, DAOs                                                                              |
| `:data:repo-room`                    | Room-backed repository implementations                                                                   |
| `:portable-data`                     | `Backup` (user data), `CollectionBundle` (deduplicated assets), `BookBundle` (single book) — YAML + gzip |

---

## 5. Where code belongs

| Code type                      | Module                 | Path                                             |
|--------------------------------|------------------------|--------------------------------------------------|
| Domain models                  | `:domain`              | `io.github.alelk.pws.domain.{entity}.model`      |
| Repository interfaces          | `:domain`              | `io.github.alelk.pws.domain.{entity}.repository` |
| Use cases                      | `:domain`              | `io.github.alelk.pws.domain.{entity}.usecase`    |
| Update/patch commands          | `:domain`              | `io.github.alelk.pws.domain.{entity}.command`    |
| Read filters/queries           | `:domain`              | `io.github.alelk.pws.domain.{entity}.query`      |
| Lyrics parser                  | `:domain:lyric-format` | `io.github.alelk.pws.domain.lyric.format`        |
| API DTOs                       | `:api:contract`        | `io.github.alelk.pws.api.contract`               |
| DTO ↔ domain mapping           | `:api:mapping`         | `io.github.alelk.pws.api.mapping`                |
| Remote repositories            | `:api:client`          | `api/client/src/commonMain/kotlin/repository/`   |
| Local repositories (Room)      | `:data:repo-room`      | `io.github.alelk.pws.data.repository.room`       |
| Feature screens + ScreenModels | `:features`            | `io.github.alelk.pws.features.{feature}`         |
| Shared UI components           | `:core:ui`             | `io.github.alelk.pws.core.ui`                    |

### Standard package layout per entity (domain)

```
{entity}/
  model/         entities, value objects (@JvmInline value class Id(...))
  repository/    {Entity}{Read|Write|Observe}Repository interfaces
  usecase/       {Action}{Entity}UseCase
  command/       UpdateXxxCommand + OptionalField<...> patches
  query/         filter/sort objects
```

### Standard feature layout

```
{feature}/
  {Feature}Screen.kt              Voyager Screen
  {Feature}ScreenModel.kt         StateScreenModel<UiState>
  {Feature}UiState.kt             sealed Loading | Content | Error
  {feature}ScreenModule.kt        Koin module
```

---

## 6. Naming conventions

| Pattern                                    | Example                    |
|--------------------------------------------|----------------------------|
| `{Action}{Entity}UseCase`                  | `GetSongDetailUseCase`     |
| `{Entity}{Read\|Write\|Observe}Repository` | `SongObserveRepository`    |
| `Remote{Entity}{Read\|Write}Repository`    | `RemoteSongReadRepository` |
| `{Feature}Screen`                          | `SongDetailScreen`         |
| `{Feature}ScreenModel`                     | `SongDetailScreenModel`    |
| `{Feature}UiState`                         | `SongDetailUiState`        |

---

## 7. Canonical patterns

### Use case with transaction boundary

```kotlin
class GetSongDetailUseCase(
    private val readRepository: SongReadRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(id: SongId): SongDetail? =
        txRunner.inRoTransaction { readRepository.get(id) }
}
```

### Reactive observe use case

```kotlin
class ObserveSongUseCase(
    private val observeRepository: SongObserveRepository
) {
    operator fun invoke(id: SongId): Flow<SongDetail?> = observeRepository.observe(id)
}
```

### Value object with validation

```kotlin
@JvmInline
value class SongId(val value: Long) {
    init {
        require(value >= 0)
    }
}
```

### Patch semantics (PATCH-style updates)

```kotlin
data class UpdateSongCommand(
    val id: SongId,
    val name: NonEmptyString? = null,                              // null = unchanged
    val author: OptionalField<Person?> = OptionalField.Unchanged   // Unchanged / Set(value) / Clear
)
```

### Screen + StateScreenModel

```kotlin
class SongDetailScreenModel(
    private val observeSong: ObserveSongUseCase,
    private val songId: SongId
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

    init {
        screenModelScope.launch(
            context = CoroutineExceptionHandler { _, _ ->
                mutableState.value = SongDetailUiState.Error
            }
        ) {
            observeSong(songId).collectLatest { detail ->
                mutableState.value = detail?.let { SongDetailUiState.Content(it) }
                    ?: SongDetailUiState.Error
            }
        }
    }
}
```

---

## 8. Hard rules (saves correction turns — don't repeat mistakes)

### Architecture

- ❌ **No `java.*` / Android / iOS APIs in `:domain`'s `commonMain`** — domain stays
  platform-agnostic.
- ❌ **UI never calls repositories** — always through use cases.
- ❌ **No cross-feature `ScreenModel` imports** — communicate via Koin-provided interfaces.
- ❌ **Do not rename DTOs in `:api:contract` / `:api:mapping`** without coordinating with
  `pws-server`.
- ✅ **Use cases own transaction boundaries** via `TransactionRunner`, not UI.

### ScreenModel / UI state *(enforced by the 2026-06 refactor,
see `docs/ai/plans/2026-06-16_features-ui-refactoring-plan.md`)*

- ✅ **One composite `XxxUiState`** per screen: `Loading | Content | Error`.
- ✅ **One-shot signals via `Channel<Effect>(BUFFERED)`** — never `MutableSharedFlow<Effect>()` for
  new code.
- ✅ **Typed errors via `UiMessage`** (`features/src/commonMain/.../app/UiMessage.kt`) — never raw
  `e.message`, never sentinel strings.
- ✅ **`koinScreenModel<T>()`** for state acquisition — never `remember { ScreenModel() }`.
- ✅ **Surface failures**: `catch (_: Exception) {}` swallows are banned in ScreenModels. Emit
  `Effect.ShowError(UiMessage.Failure(...))` or update state.
- ✅ **Parameterised `Screen`** is `data class` OR has `override val key = "stable/$param"`.

### Compose

- ❌ **No `Modifier.composed { }`** (deprecated). Use Composable factory pattern (
  `@Composable fun Modifier.xxx(...)`).
- ❌ **No hardcoded UI strings** — always `stringResource(Res.string.*)`.
- ❌ **No localised text in `ScreenModel` state or effects** — that's the i18n boundary.
- ✅ **One `ActiveSheet?` sealed bucket** when a screen owns multiple bottom sheets; not N booleans.

### Books visibility

- ✅ For UI lists that must hide disabled books, use `BookQuery(enabled = true)`.

---

## 9. Workflows (checklists)

### Add a new use case

1. Create use case in `domain/.../usecase/`.
2. Inject **repository interfaces**, not concrete implementations.
3. Add `TransactionRunner` if it reads/writes local data.
4. Add or update tests (`:domain:domain-test-fixtures` has generators).

### Add or change a feature screen

1. Update `Screen`, `ScreenModel`, `UiState` in `:features`.
2. Wire Koin module next to the feature.
3. Keep UI passive; business logic in use cases.
4. Validate behavior against feature docs (`docs/features/*.md`).

### Change API contracts

1. Update DTOs in `:api:contract`.
2. Update mappings in `:api:mapping`.
3. **Coordinate with `pws-server`** — note migration impact.
4. Document migration notes.

### General working order

1. Confirm behavior in `docs/FEATURES.md` and `docs/features/*.md`.
2. Confirm module boundaries in `docs/MODULES.md` and `docs/ARCHITECTURE.md`.
3. Implement in the correct module (use the table above).
4. Run module-scoped tests for touched modules.
5. Update docs **if** behavior/contracts/architecture changed.

---

## 10. Documentation index (read on demand — do NOT preload)

| When you need…                                  | Open                                                                           |
|-------------------------------------------------|--------------------------------------------------------------------------------|
| Doc directory overview                          | [`docs/README.md`](docs/README.md)                                             |
| Architecture details                            | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)                                 |
| Module inventory & dependency graph             | [`docs/MODULES.md`](docs/MODULES.md)                                           |
| Feature behavior catalog                        | [`docs/FEATURES.md`](docs/FEATURES.md), [`docs/features/*.md`](docs/features/) |
| Data/API flow & content delivery (bundles)      | [`docs/DATA_FLOW.md`](docs/DATA_FLOW.md)                                       |
| Sync design notes (not yet implemented)         | [`docs/SYNC.md`](docs/SYNC.md)                                                 |
| Domain terminology                              | [`docs/GLOSSARY.md`](docs/GLOSSARY.md)                                         |
| **In-flight plans (read first when relevant!)** | [`docs/ai/plans/`](docs/ai/plans/)                                             |
| Plan template for new initiatives               | [`docs/ai/TASK_PLAN_TEMPLATE.md`](docs/ai/TASK_PLAN_TEMPLATE.md)               |
| Claude-specific niceties (skills, etc.)         | [`CLAUDE.md`](CLAUDE.md)                                                       |

---

## 11. Cross-repo composite build

When this repo is checked out **next to** `pws-android`, Gradle's composite build links`../pws-core`
automatically (see `pws-android/settings.gradle.kts`). Changes here propagate to the Android build
without `publishToMavenLocal`. If a user reports an Android-only symptom, check **both** repos.

---

## 12. Don't waste tokens on

These paths are noise — never grep, list, or read them unless the user explicitly references them:

- `build/`, `*/build/`, `.gradle/`, `.kotlin/`
- `local-repo/`, `kotlin-js-store/`
- `*.iml`, `.idea/`
- Generated Compose resource accessors under `features/build/generated/`

---

*Maintenance note: when conventions, modules, or hard rules change, update this file. If a section
grows past one screen, split it into `docs/` and link from here.*

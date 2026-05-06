# Instructions for AI Agents

This file describes how to implement changes in `pws-core` safely and consistently.
Read `docs/ai/CONTEXT.md` first for project context.

## Working order

1. Confirm behavior in `docs/FEATURES.md` and `docs/features/*.md`.
2. Confirm architecture/module boundaries in `docs/ARCHITECTURE.md` and `docs/MODULES.md`.
3. Implement changes in the correct module.
4. Run relevant tests/checks for changed modules.
5. Update docs if behavior/contracts changed.

## Where code belongs

| Code type | Module | Typical package/path |
|---|---|---|
| Domain models | `:domain` | `io.github.alelk.pws.domain.{entity}.model` |
| Domain repository interfaces | `:domain` | `io.github.alelk.pws.domain.{entity}.repository` |
| Domain use cases | `:domain` | `io.github.alelk.pws.domain.{entity}.usecase` |
| Lyrics parser | `:domain:lyric-format` | `io.github.alelk.pws.domain.lyric.format` |
| API DTOs | `:api:contract` | `io.github.alelk.pws.api.contract` |
| DTO-domain mapping | `:api:mapping` | `io.github.alelk.pws.api.mapping` |
| Remote repositories | `:api:client` | `api/client/src/commonMain/kotlin/repository/` |
| Local repositories (Room) | `:data:repo-room` | `io.github.alelk.pws.data.repository.room` |
| Feature screens/screen models | `:features` | `io.github.alelk.pws.features.{feature}` |
| Shared UI components | `:core:ui` | `io.github.alelk.pws.core.ui` |

## Naming conventions

- Use case: `{Action}{Entity}UseCase`
- Repository interface: `{Entity}{Read|Write|Observe}Repository`
- Screen state holder: `{Feature}ScreenModel` (Voyager `StateScreenModel`)
- Screen entrypoint: `{Feature}Screen`
- UI state: `{Feature}UiState`
- Remote implementation: `Remote{Entity}{Read|Write}Repository`

## Implementation patterns

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

### Observe use case

```kotlin
class ObserveSongUseCase(
  private val observeRepository: SongObserveRepository
) {
  operator fun invoke(id: SongId): Flow<SongDetail?> = observeRepository.observe(id)
}
```

### Screen + `StateScreenModel`

```kotlin
class SongDetailScreenModel(
  private val observeSong: ObserveSongUseCase,
  private val songId: SongId
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  init {
    screenModelScope.launch(
      context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }
    ) {
      observeSong(songId).collectLatest { detail ->
        mutableState.value = detail?.let { SongDetailUiState.Content(it) } ?: SongDetailUiState.Error
      }
    }
  }
}
```

### Update commands with patch semantics

```kotlin
data class UpdateSongCommand(
  val id: SongId,
  val name: NonEmptyString? = null,
  val author: OptionalField<Person?> = OptionalField.Unchanged
)
```

## Rules that prevent regressions

- Do not call repositories directly from UI; always go through use cases.
- Do not add Android/iOS-specific APIs to `commonMain` domain code.
- Keep contracts in `:api:contract` and `:api:mapping` compatible with `pws-server`.
- Avoid cross-feature tight coupling in `:features`.
- For behavior that depends on enabled/disabled books, use `BookQuery(enabled = true)` where appropriate.

## Common task checklists

### Add a new use case

1. Create use case in `domain/.../usecase/`.
2. Inject repository interfaces (not concrete implementations).
3. Add transaction runner if it reads/writes local data.
4. Add or update tests in the relevant module.

### Add or change a feature screen

1. Update `Screen`, `ScreenModel`, and `UiState` in `:features`.
2. Wire dependencies in Koin module next to the feature.
3. Keep UI passive; place business logic in use cases.
4. Validate behavior against feature docs.

### Change API contracts

1. Update DTOs in `:api:contract`.
2. Update mappings in `:api:mapping`.
3. Check compatibility impact on `pws-server`.
4. Document any migration notes.

## Verification commands

```shell
./gradlew :domain:jvmTest
./gradlew :portable-data:jvmTest
./gradlew :data:db-room:testDebugUnitTest :data:db-room:jvmTest
```

Use module-scoped tasks for touched modules first; run broader checks only when needed.

Last reviewed: 2026-04-29

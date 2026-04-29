# PWS Core - Guide for AI Agents

Use this file as a fast operational runbook.
For detailed context and conventions, continue with `docs/ai/CONTEXT.md` and `docs/ai/CONTRIBUTING.md`.

## Mission and boundaries

- Repository role: multiplatform core library for PWS (domain + UI + API client + local data).
- Related repos: `pws-server` (backend API), `pws-android` (Android app using this library).
- Important contract rule: `:api:contract` and `:api:mapping` must stay compatible with `pws-server`.

## What to read for each task

- Feature behavior or business rules: `docs/FEATURES.md`, `docs/features/*.md`.
- Architecture and dependencies: `docs/ARCHITECTURE.md`, `docs/MODULES.md`.
- API/data flow questions: `docs/DATA_FLOW.md`, `docs/SYNC.md`.
- Terminology: `docs/GLOSSARY.md`.

## Code navigation shortcuts

- Domain models: `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/model/`
- Domain use cases: `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/usecase/`
- Domain repositories: `domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/repository/`
- Features (screens/screen models): `features/src/commonMain/kotlin/io/github/alelk/pws/features/{feature}/`
- Remote repositories: `api/client/src/commonMain/kotlin/repository/`
- Room repositories: `data/repo-room/src/commonMain/kotlin/io/github/alelk/pws/data/repository/room/`

## Core implementation patterns

```kotlin
// Use case with transaction boundary
class GetSongDetailUseCase(
  private val readRepository: SongReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: SongId): SongDetail? =
    txRunner.inRoTransaction { readRepository.get(id) }
}

// Observe use case (reactive path)
class ObserveSongUseCase(
  private val observeRepository: SongObserveRepository
) {
  operator fun invoke(id: SongId): Flow<SongDetail?> = observeRepository.observe(id)
}

// Feature state holder uses Voyager ScreenModel
class BooksScreenModel(
  private val observeBooks: ObserveBooksUseCase
) : StateScreenModel<BooksUiState>(BooksUiState.Loading)
```

## Working checklist (before coding)

- Confirm target module and package before creating files.
- Reuse existing commands/queries/use cases when possible.
- Keep domain platform-agnostic (`commonMain` without Android/iOS APIs).
- For UI changes, follow `Screen` + `StateScreenModel` + `UiState` pattern.

## Working checklist (before finalizing)

- Check compile/lint/test for changed modules.
- Verify behavior changes against feature docs.
- If API contracts changed, note required alignment with `pws-server`.
- Keep docs updated when changing architecture, flows, or conventions.

## Do not do

- Do not call repositories directly from UI; go through use cases.
- Do not add platform-specific code into `:domain`.
- Do not introduce cross-feature tight coupling.
- Do not rename API DTO fields without contract review.

Last reviewed: 2026-04-29

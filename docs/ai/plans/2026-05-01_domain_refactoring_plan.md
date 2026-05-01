# 2026-05-01 Domain Refactoring Plan

## Meta

- Initiative: Domain Refactoring for Clean Architecture Alignment
- Date: 2026-05-01
- Owner/Agent: Junie
- Repositories in scope: pws-core, pws-server
- Related issue/PR: N/A
- Objective (1-2 lines): Align domain architecture with Clean Architecture by moving business logic from repositories to use cases and standardizing return types with Either.

## Definition of Done

- [x] ReadError defined in pws-core:domain.
- [x] Tag Use Cases refactored (Existence/Uniqueness moved to UC).
- [x] Song Use Cases refactored (Existence/Version/OptionalField logic moved to UC/Domain).
- [x] Read Use Cases (Book, Song, Tag) return Either<ReadError, T>.
- [x] Repositories in pws-server (Exposed) and pws-core (Room) simplified.
- [x] All tests in pws-core and pws-server pass.

## Scope

### In scope

- pws-core:domain (Use cases, models, errors)
- pws-core:data:repo-room (Repository implementations)
- pws-server:infra (Repository implementations)

### Out of scope

- UI components
- API DTOs (unless mapping requires changes due to Either)

## Task Board

| Checkbox | ID | Priority | Status | Task | Target modules/files | Acceptance criteria |
|---|---|---|---|---|---|---|
| [x] | T-001 | must | DONE | Define ReadError | pws-core:domain | sealed interface with NotFound and UnknownError |
| [x] | T-002 | must | DONE | Refactor Tag Use Cases | pws-core:domain:tag | UC checks existence/uniqueness before repo call |
| [x] | T-003 | must | DONE | Refactor Song Use Cases | pws-core:domain:song | UC handles versioning and OptionalField logic |
| [x] | T-004 | must | DONE | Simplify Tag Repositories | pws-server, pws-core | Remove logic from Exposed/Room implementations |
| [x] | T-005 | must | DONE | Simplify Song Repositories | pws-server, pws-core | Remove logic from Exposed/Room implementations |
| [x] | T-006 | should | DONE | Standardize Read Use Cases | pws-core:domain | Return Either instead of nullable |

## Context Updates (append-only)

2026-05-01 00:15 | Junie | Created plan | Initializing refactoring | ../pws-core/docs/ai/plans/2026-05-01_domain_refactoring_plan.md
2026-05-01 15:20 | Copilot | Added domain write-UC tests (song/tag) and aligned Song update patch path | Stabilized UC-level business rules; reduced room delete read-before-write checks via deleteById | domain/src/commonTest/kotlin/io/github/alelk/pws/domain/song/usecase/SongWriteUseCasesTest.kt, domain/src/commonTest/kotlin/io/github/alelk/pws/domain/tag/usecase/TagWriteUseCasesTest.kt, data/db-room/src/commonMain/kotlin/io/github/alelk/pws/database/song/SongDaoBase.kt, data/db-room/src/commonMain/kotlin/io/github/alelk/pws/database/tag/TagDao.kt
2026-05-01 16:05 | Copilot | Finished repository thinning (Song/Tag) and completed final verification matrix | Exposed+Room Song/Tag write repositories aligned with thin-repository principle; full pws-server tests and extended pws-core matrix green | pws-server/infra/src/main/kotlin/repository/song/SongWriteRepositoryImpl.kt, pws-server/infra/src/main/kotlin/repository/tag/TagWriteRepositoryImpl.kt, pws-core/data/repo-room/src/commonMain/kotlin/io/github/alelk/pws/data/repository/room/song/SongRepositoryImpl.kt, pws-core/data/repo-room/src/commonMain/kotlin/io/github/alelk/pws/data/repository/room/tag/TagRepositoryImpl.kt

## Decision Log

- Decision: Specific Error Types
  - Chosen: Separate ReadError, CreateError, UpdateError, DeleteError.
  - Alternatives: Single DomainError.
  - Reason: Preferred by user for better specificity.

- Decision: Business Logic Location
  - Chosen: Use Cases (Orchestrators).
  - Alternatives: Repositories (current).
  - Reason: Align with Clean Architecture; logic should be DB-agnostic.

## Handoff (for next agent)

### Current status

- Completed: Step 1 (Analysis), ReadError rollout for major read UCs, Song/Tag write-UC behavior tests, repository thinning for Song/Tag in pws-core and pws-server
- In progress: Follow-up tech debt only (optional) around full removal of deprecated legacy result wrappers
- Blocked: None

### Next 1-3 steps

1. (Optional) Replace deprecated legacy `domain.core.result.*` wrappers with pure `Either` migration docs and removal strategy.
2. (Optional) Add additional integration tests for race conditions around create/update conflicts.
3. Prepare release notes for architecture refactoring completion.

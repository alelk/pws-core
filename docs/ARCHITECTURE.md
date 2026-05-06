# PWS Core Architecture

## Overview

`pws-core` follows clean architecture with strict dependency direction:

```text
UI (Compose Screen)
  -> StateScreenModel (Voyager)
    -> UseCase (domain)
      -> Repository interface (domain)
        -> Local impl (Room) OR Remote impl (Ktor)
```

## Layers

### Presentation (`:features`, `:core:navigation`, `:core:ui`)

- `Screen` renders UI.
- `StateScreenModel` holds state/effects and calls use cases.
- Navigation contracts are defined in `core/navigation/src/commonMain/kotlin/SharedScreens.kt`.

### Domain (`:domain`)

- Source of business rules.
- Contains models, commands/queries, use cases, repository interfaces.
- Stays platform-agnostic (`commonMain` only, no Android/iOS APIs).

### Data implementations

- Local path: `:data:db-room` + `:data:repo-room`.
- Remote path: `:api:client` + `:api:mapping` + `:api:contract`.

### Supporting modules

- `:domain:lyric-format`: parsing/formatting lyrics.
- `:domain:domain-test-fixtures`: generators/helpers for tests.
- `:portable-data`: portable serialisation formats — `Backup` (user data export/import), `CollectionBundle` (full deduplicated collection for asset delivery), `BookBundle` (single book for dynamic delivery via Play Asset Delivery / CDN). Format: YAML (kaml) + gzip on JVM/Native.

## Dependency rules

- UI depends on domain contracts, not on concrete data sources.
- Repository interfaces live in domain; implementations live in data/api modules.
- Use cases own transaction boundaries (`TransactionRunner`) for consistency.
- DTO contracts (`:api:contract`) and mappings (`:api:mapping`) must stay compatible with `pws-server`.

## Runtime data source selection

- Android/iOS typically use Room-backed repositories.
- Web/Telegram Mini App typically use remote API repositories.
- DI (Koin) decides concrete bindings per target app.

## Reactive model

- Read paths are mostly `Flow`-based (`Observe*UseCase`).
- Screen models collect flows and convert them to `UiState`.
- Write operations go through `*WriteRepository` and expose typed results.

## Core UI pattern

```kotlin
class FeatureScreenModel(
  private val observeData: ObserveSomethingUseCase
) : StateScreenModel<FeatureUiState>(FeatureUiState.Loading)
```

Use this as the default presentation pattern unless there is a strong reason not to.

## Related documents

- Module details: `docs/MODULES.md`
- Data/API flow: `docs/DATA_FLOW.md`
- Feature behavior: `docs/FEATURES.md` and `docs/features/*.md`

Last reviewed: 2026-05-06

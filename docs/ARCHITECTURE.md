# Architecture

Deep-dive on layer responsibilities and dependency rules.
For the one-screen overview, module table, and canonical patterns see [`../AGENTS.md`](../AGENTS.md).

---

## Layer responsibilities

### Presentation — `:features`, `:core:navigation`, `:core:ui`

- `Screen` (Voyager) — renders UI; no business logic.
- `StateScreenModel<UiState>` — holds state, owns one-shot `Channel<Effect>(BUFFERED)`, calls use cases.
- `:core:navigation` defines shared navigation contracts (`SharedScreens`) — features depend on this, not on each other.
- `:core:ui` — primitives that don't belong to a specific feature.

UI is **passive**: no calls to repositories, no transactions, no localised text inside state/effects (i18n boundary).

### Domain — `:domain`

Source of business rules. Contains:
- Models, value objects (validated `@JvmInline value class` IDs).
- Repository **interfaces** only (no implementations).
- Use cases (`{Action}{Entity}UseCase`) — single responsibility, own the transaction boundary via `TransactionRunner`.
- Commands (`UpdateXxxCommand` + `OptionalField<T>` for PATCH semantics) and queries (filter/sort objects).

Strict constraint: `commonMain` only, no `java.*` / Android / iOS APIs.

### Data implementations — local + remote

| Path   | Modules                                              | Used by      |
|--------|------------------------------------------------------|--------------|
| Local  | `:data:db-room` (schema) + `:data:repo-room` (impls) | Android, iOS |
| Remote | `:api:contract` + `:api:mapping` + `:api:client`     | Web, Telegram Mini App |

DI (Koin) decides concrete bindings per host app — domain doesn't care which one is wired.

### Supporting modules

- `:domain:lyric-format` — lyrics parser/formatter.
- `:domain:domain-test-fixtures` — generators/helpers (use these in domain tests).
- `:portable-data` — portable serialisation formats (YAML + gzip on JVM/Native):
  - `Backup` — user-data export/import.
  - `CollectionBundle` — full deduplicated collection for asset delivery.
  - `BookBundle` — single book for dynamic delivery (Play Asset Delivery / CDN).

---

## Dependency rules

1. **UI depends on domain contracts**, not concrete data sources.
2. **Repository interfaces live in `:domain`**, implementations in `:data:repo-room` / `:api:client`.
3. **Use cases own transaction boundaries** (`TransactionRunner`).
4. **`:api:contract` + `:api:mapping`** must stay compatible with `pws-server` — coordinate any DTO/field rename.
5. **No cross-feature `ScreenModel` imports** — communicate via Koin-provided interfaces or navigation contracts.

The forbidden direction is anything that lets `:domain` import from `:features`, `:api:client`, `:data:repo-room`, or platform-specific code.

---

## Reactive model

- Read paths return `Flow` via `Observe*UseCase`.
- ScreenModels collect flows and produce a composite `XxxUiState` (`Loading | Content | Error`).
- Writes go through `*WriteRepository` and return typed sealed results — not exceptions for expected business outcomes.
- One-shot signals (snackbar, navigation effect, dialog open) use `Channel<Effect>(BUFFERED)` — never `MutableSharedFlow<Effect>()` for new code.

---

## Runtime data source selection

- Android/iOS: Room-backed repositories (offline-first).
- Web/Telegram Mini App: remote API repositories (no offline).
- The choice is wired in the host app's Koin modules; `pws-core` itself ships both sets.

---

## Related

- [`MODULES.md`](MODULES.md) — dependency graph + code locations
- [`DATA_FLOW.md`](DATA_FLOW.md) — local vs remote routing + content bundles
- [`FEATURES.md`](FEATURES.md) — feature catalog
- [`../AGENTS.md`](../AGENTS.md) — operational runbook

Last reviewed: 2026-06-17

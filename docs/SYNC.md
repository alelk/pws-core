# Sync (design notes — NOT IMPLEMENTED)

> ⚠️ **Status:** there is no `:sync` module in `pws-core` today. Mobile targets are offline-first
> via local Room storage. This file captures the intended direction and constraints so that the
> eventual implementation stays consistent with the rest of the architecture. **Do not assume any of
this is wired up.**

---

## Goals

- Keep UX responsive offline.
- Reconcile local user data with the server when network/auth become available.
- Avoid data loss for favorites / history / tags / user overrides.

## Candidate sync scope

| Data group                                    | Direction       | Priority |
|-----------------------------------------------|-----------------|----------|
| Read-only catalog (songs, books, global tags) | Server → Client | Medium   |
| User data (favorites, history, user tags)     | Bidirectional   | High     |
| User overrides                                | Bidirectional   | High     |

## Intended architecture

```text
UI → UseCase → Local repository (Room)
                       │
                       ▼
                 Sync coordinator
                 ┌──────┴──────┐
        Local change log   Remote API (Ktor)
```

## Conflict strategy draft

| Entity              | Preferred strategy        |
|---------------------|---------------------------|
| Favorites           | Last-write-wins           |
| History             | Merge append-only entries |
| User tags           | Merge + LWW for edits     |
| User song overrides | Last-write-wins           |

## Sync triggers (candidate)

- Connectivity restored.
- App foreground after staleness threshold.
- Manual refresh.
- Optional background scheduler per host app.

## Constraints

- Domain layer must remain platform-agnostic.
- Sync-specific implementation lives **outside `:domain`** (proposed `:sync` module).
- Any new API endpoints must stay aligned with `pws-server`.

## Implementation roadmap (when work starts)

1. Define minimal sync interfaces and a status model.
2. Add a persistent queue for local mutations that require push.
3. Implement push-first flow for favorites/history.
4. Add a pull-and-merge policy per entity.
5. Add telemetry / error visibility for host apps.

Last reviewed: 2026-06-17

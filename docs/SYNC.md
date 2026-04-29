# Synchronization (Status and Design Notes)

## Current status

- A dedicated sync module is **not implemented** in `pws-core` at this time.
- Mobile targets still follow offline-first behavior via local storage as primary source.
- This file describes intended synchronization direction and constraints.

## Goals

- Keep UX responsive offline.
- Reconcile local user data with server when network/auth are available.
- Avoid data loss for favorites/history/tags/user overrides.

## Candidate sync scope

| Data group | Direction | Priority |
|---|---|---|
| Read-only catalog (songs/books/global tags) | Server -> Client | Medium |
| User data (favorites/history/user tags) | Bidirectional | High |
| User overrides | Bidirectional | High |

## Intended architecture (conceptual)

```text
UI -> UseCase -> Local repository (Room)
                       |
                       v
                 Sync coordinator
                 /             \
         Local change log    Remote API
```

## Conflict strategy draft

| Entity | Preferred strategy |
|---|---|
| Favorites | Last-write-wins |
| History | Merge append-only entries |
| User tags | Merge + LWW for edits |
| User song overrides | Last-write-wins |

## Triggers (candidate)

- Connectivity restored.
- App foreground after staleness threshold.
- Manual refresh.
- Optional background scheduler per host app.

## Constraints

- Keep domain layer platform-agnostic.
- Synchronization-specific implementation details should live outside `:domain`.
- Any API changes required for sync must remain aligned with `pws-server` contracts.

## Next implementation milestones

1. Define minimal sync interfaces and status model.
2. Add persistent queue for local mutations that require push.
3. Implement push-first flow for favorites/history.
4. Add pull-and-merge policy per entity.
5. Add telemetry/error visibility for host apps.

Last reviewed: 2026-04-29

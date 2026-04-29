# PWS Features

This document is an index of implemented behavior.
Detailed behavior and edge cases are documented in `docs/features/*.md`.

## Core feature set

### Search

- Full-text search by text/number.
- Search suggestions with debounce in UI.
- Suggestions/results can include `bookReferences` for context navigation.

Details: `docs/features/search.md`

### Books and songs

- Books list and songs-in-book flows.
- Song detail screen with lyric + metadata.
- Book visibility can be filtered by enabled priority (`BookQuery(enabled = true)`).

### Favorites

- Toggle favorite from song screen.
- Favorites list is reactive via observe use cases.

Details: `docs/features/favorites.md`

### History

- Song views are recorded with debounce/time threshold in screen logic.
- History list supports removal and clear actions.

Details: `docs/features/history.md`

### Tags

- Global and custom tags.
- CRUD for user tags.
- Tag-to-song assignment via replace-all semantics.

Details: `docs/features/tags.md`

### User song overrides

- Users can override global song fields.
- Merged song view is returned for display.
- Override reset is supported.

Details: `docs/features/user-overrides.md`

## Platform notes

| Capability | Android/iOS | Web/Telegram Mini App |
|---|---|---|
| Main UI flows | Yes | Yes |
| Primary data source | Local Room | Remote API |
| Offline-first behavior | Yes | No |

## Related docs

- Architecture: `docs/ARCHITECTURE.md`
- Data flow/API: `docs/DATA_FLOW.md`
- Synchronization status: `docs/SYNC.md`

Last reviewed: 2026-04-29

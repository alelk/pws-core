# Features

Index of implemented behavior. Each row links to a deep-dive in [`features/`](features/) with edge cases, use cases, and related files.

| Feature            | Summary                                                                                            | Deep dive                                                   |
|--------------------|----------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| **Search**         | Full-text + number search; debounced suggestions with `bookReferences` for book-context navigation | [`features/search.md`](features/search.md)                  |
| **Books & songs**  | Books list, songs-in-book, song detail; visibility filtered via `BookQuery(enabled = true)`        | (see `:features/book`, `:features/books`, `:features/song`) |
| **Favorites**      | Toggle from song screen; reactive list; two subject types (`BookedSong` / `StandaloneSong`)        | [`features/favorites.md`](features/favorites.md)            |
| **History**        | Records song views (debounce/threshold in screen logic); reactive list; remove/clear actions       | [`features/history.md`](features/history.md)                |
| **Tags**           | Global + custom tags; CRUD for user tags; replace-all semantics for song-tag assignments           | [`features/tags.md`](features/tags.md)                      |
| **User overrides** | Per-user song field overrides without mutating canonical data; merged view via `MergedSongDetail`  | [`features/user-overrides.md`](features/user-overrides.md)  |

---

## Platform notes

| Capability             | Android / iOS | Web / Telegram Mini App |
|------------------------|---------------|-------------------------|
| Main UI flows          | ✅            | ✅                      |
| Primary data source    | Local Room    | Remote API              |
| Offline-first behavior | ✅            | ❌                      |

---

## Related

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — layer responsibilities
- [`DATA_FLOW.md`](DATA_FLOW.md) — local vs remote routing, search behavior, bundle delivery
- [`SYNC.md`](SYNC.md) — sync design notes (not implemented)

Last reviewed: 2026-06-17

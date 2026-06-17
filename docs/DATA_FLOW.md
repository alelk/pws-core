# Data Flow

How data moves through layers and across local/remote sources.
For the architectural overview see [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## Local vs. remote routing

| Host target             | Primary source | Modules used                                     |
|-------------------------|----------------|--------------------------------------------------|
| Android / iOS           | Local Room     | `:data:db-room` + `:data:repo-room`              |
| Web / Telegram Mini App | Remote API     | `:api:client` + `:api:contract` + `:api:mapping` |

DI (Koin) binds the concrete repository per host. Domain code never knows which one is wired.

---

## Read path

```text
Observe*UseCase → Flow<T> → ScreenModel state → UI recomposition
Get*UseCase     → suspend → ScreenModel single fetch
```

## Write path

```text
User action → UseCase (rw transaction) → WriteRepository
                                          ├─ persists to DB / API
                                          └─ observed flows emit updated state
```

`UseCase` owns the transaction (`TransactionRunner.inTransaction { … }`). UI never opens
transactions.

---

## Mapping boundaries

- Transport types — `:api:contract` (DTOs + Ktor `@Resource` definitions).
- DTO ↔ domain — `:api:mapping`.
- Room entities / DAO projections — internal to `:data:db-room`.

**Rule:** domain models carry business semantics; transport and storage models are adapters and may
be reshaped freely as long as the mapping layer stays in sync.

---

## API contract source of truth

Don't duplicate endpoint lists here. Browse contract resources under:

`api/contract/src/commonMain/kotlin/{book,song,tag,favorite,history,usersong,usertag,userbook,admin}/`

---

## Search

- `SearchSongsUseCase` returns `SongSearchResponse` (full results).
- `SearchSongSuggestionsUseCase` returns `SongSearchSuggestion` (lightweight).
- Suggestions include `bookReferences` so UI can navigate using `SongNumberId(bookId, songId)` when
  available.
- ScreenModels apply **debounce** before issuing requests.

Navigation rule on result click:

1. If `bookReferences` is non-empty → navigate by `SongNumberId(bookId, songId)`.
2. Otherwise → navigate by `SongId` only.

---

## Book visibility

Books are priority-driven. For UI lists that must hide disabled books:

```kotlin
BookQuery(enabled = true)
```

---

## Asset-based content delivery (`CollectionBundle`)

`pws-android` can initialise the Room database from a bundled `*.collection.yaml.gz` file shipped in
`assets/library/`.

```text
pws-v2x-library-manager
  ./pws-mgr library export-bundle <lib-file> --output <dir>
    → {locale}.collection.yaml.gz

pws-android  (cold start)
  DatabaseInitializer
    reads assets/library/{locale}.collection.yaml.gz
    BundleSerializer.decodeCollectionGzip(bytes) : CollectionBundle
    writes → Room DAOs (idempotent)
```

- `CollectionBundle` mirrors `BookCollection` from `pws-v2x-library-manager`: songs are *
  *deduplicated** across books — each `Song.number` may reference multiple books.
- `BookBundle` carries a single book — used for dynamic delivery (Play Asset Delivery / CDN).
- Both formats serialise as YAML + gzip via `BundleSerializer` in `:portable-data`.

For Android-side asset encryption and Keystore-backed DB passphrase see [
`pws-android/docs/data-security.md`](../../pws-android/docs/data-security.md).

---

## Consistency checklist for data-flow changes

When changing how data moves, verify all touched layers:

1. Domain contracts (`:domain`).
2. API contract + mapping (`:api:contract`, `:api:mapping`) if the remote shape changed → coordinate
   with `pws-server`.
3. Room schema / DAO (`:data:db-room`, `:data:repo-room`) if local behavior changed.
4. ScreenModel usage in `:features`.
5. Portable bundles (`:portable-data`) if the DB schema or content-delivery format changed.

Last reviewed: 2026-06-17

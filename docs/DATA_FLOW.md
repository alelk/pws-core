# Data Flows and API

## Overview

`pws-core` supports two runtime data sources selected by DI in the host app:

| Target | Primary source | Implementation modules |
|---|---|---|
| Android/iOS | Room | `:data:db-room` + `:data:repo-room` |
| Web/Telegram Mini App | Remote API | `:api:client` (+ `:api:contract`, `:api:mapping`) |

## End-to-end flow

```text
Screen
  -> StateScreenModel
    -> UseCase
      -> Domain repository interface
        -> Local repo OR Remote repo
```

### Read flow

```text
Observe*UseCase -> Flow<T> -> ScreenModel state -> UI recomposition
```

### Write flow

```text
User action -> UseCase (rw transaction) -> WriteRepository -> DB/API
                                            -> observed flows emit updated state
```

## Mapping boundaries

- API transport types live in `:api:contract`.
- Conversion between DTO and domain lives in `:api:mapping`.
- Room entities/DAO projections are internal to local storage modules.

Rule: domain models are source of business semantics; transport/storage models are adapters.

## API contracts (source of truth)

Do not treat this page as a full endpoint reference. Use contract resources in:

- `api/contract/src/commonMain/kotlin/book/`
- `api/contract/src/commonMain/kotlin/song/`
- `api/contract/src/commonMain/kotlin/tag/`
- `api/contract/src/commonMain/kotlin/favorite/`
- `api/contract/src/commonMain/kotlin/history/`
- `api/contract/src/commonMain/kotlin/usersong/`
- `api/contract/src/commonMain/kotlin/usertag/`
- `api/contract/src/commonMain/kotlin/userbook/`
- `api/contract/src/commonMain/kotlin/admin/`

## Search-specific notes

- Search use cases return domain search models (`SongSearchResponse`, `SongSearchSuggestion`).
- Suggestions include `bookReferences` so UI can navigate with `SongNumberId` when available.
- Screen models apply debounce before remote/local search requests.

## Books visibility notes

- Book visibility is priority-driven.
- For UI lists that must hide disabled books, use `BookQuery(enabled = true)`.

## Consistency checklist for changes

When touching data flow, verify all affected layers:

1. Domain contracts (`:domain`).
2. API contract and mapping (`:api:contract`, `:api:mapping`) if remote shape changed.
3. Local repo/DAO path (`:data:repo-room`, `:data:db-room`) if local behavior changed.
4. ScreenModel usage in `:features`.
5. Portable bundle (`：portable-data`) if the local DB schema or content delivery format changed.

## Asset-based content delivery (CollectionBundle)

`pws-android` can initialise the Room database from a bundled `*.collection.yaml.gz` file placed in `assets/library/`.

```text
pws-v2x-library-manager
  ./pws-mgr library export-bundle <lib-file> --output <dir>
    → {locale}.collection.yaml.gz

pws-android (cold start)
  DatabaseInitializer
    reads assets/library/{locale}.collection.yaml.gz
    BundleSerializer.decodeCollectionGzip(bytes): CollectionBundle
    writes → Room DAOs (idempotent)
```

- `CollectionBundle` mirrors `BookCollection` from `pws-v2x-library-manager`: songs are **deduplicated** across books, each `Song.number` may reference multiple books.
- `BookBundle` contains a single book — used for dynamic delivery (Play Asset Delivery / CDN).
- Both formats serialise as YAML + gzip via `BundleSerializer` in `:portable-data`.

Last reviewed: 2026-05-06

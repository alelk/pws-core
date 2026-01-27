# PWS Core Architecture

## Overview

PWS Core uses **Clean Architecture** with clear layer separation:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│                   (features, navigation)                    │
│    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│    │   Screen    │ │  ViewModel  │ │   Screen    │          │
│    └─────────────┘ └─────────────┘ └─────────────┘          │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│                        (domain)                             │
│    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│    │   UseCase   │ │   Model     │ │  Repository │          │
│    │             │ │             │ │  Interface  │          │
│    └─────────────┘ └─────────────┘ └─────────────┘          │
└────────────────────────────┬────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
┌─────────────────────────┐   ┌─────────────────────────────┐
│      Data Layer         │   │       Data Layer            │
│   (repo-room, db-room)  │   │      (api/client)           │
│                         │   │                             │
│  ┌───────────────────┐  │   │  ┌───────────────────────┐  │
│  │ LocalRepository   │  │   │  │  RemoteRepository     │  │
│  │ (Room Database)   │  │   │  │  (Ktor HTTP Client)   │  │
│  └───────────────────┘  │   │  └───────────────────────┘  │
│                         │   │                             │
│  Android/iOS            │   │  Web/Telegram Mini App      │
└─────────────────────────┘   └─────────────────────────────┘
```

## Dependency Injection (Koin)

The application uses Koin for DI. Repositories are connected depending on the platform:

```kotlin
// For Android/iOS (local DB)
val localModule = module {
    single<SongReadRepository> { RoomSongReadRepository(get()) }
}

// For Web/TG Mini App (remote API)
val remoteModule = module {
    single<SongReadRepository> { RemoteSongReadRepository(get()) }
}
```

Use Cases receive repositories through DI and are unaware of the implementation.

## Data Flows

### Reading Data (Query)

```
User Action
    │
    ▼
┌─────────┐      ┌───────────┐      ┌────────────┐      ┌──────────┐
│ Screen  │ ──▶  │ ViewModel │ ──▶  │  UseCase   │ ──▶  │Repository│
└─────────┘      └───────────┘      └────────────┘      └──────────┘
    ▲                  │                                     │
    │                  │                                     │
    └──────────────────┴──────────── Flow<Data> ─────────────┘
```

### Writing Data (Command)

```
User Action
    │
    ▼
┌─────────┐      ┌───────────┐      ┌────────────┐      ┌──────────┐
│ Screen  │ ──▶  │ ViewModel │ ──▶  │  UseCase   │ ──▶  │Repository│
└─────────┘      └───────────┘      └────────────┘      └──────────┘
                       │                                     │
                       ▼                                     ▼
                 ┌───────────┐                       ┌──────────┐
                 │UI Updated │ ◀───── Flow Update ───│  DB/API  │
                 └───────────┘                       └──────────┘
```

## Navigation (Voyager)

Voyager is used for multiplatform navigation.

### SharedScreens

Defined in `core/navigation`:

```kotlin
// SharedScreens.kt
sealed interface SharedScreen {
    data class Song(val songId: Long) : SharedScreen
    data class Book(val bookId: String) : SharedScreen
    data class Tag(val tagId: Long) : SharedScreen
    object Favorites : SharedScreen
    object History : SharedScreen
    object Search : SharedScreen
}
```

### Screen Implementation

Each screen in the `features` module:

```kotlin
class SongScreen(private val songId: Long) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<SongViewModel>()
        // ...
    }
}
```

## Reactivity

### Flow for Data

Observe repositories return `Flow` for reactive updates:

```kotlin
interface SongObserveRepository {
    fun observe(id: SongId): Flow<SongDetail?>
    fun observeAllInBook(bookId: BookId): Flow<Map<Int, SongSummary>>
}
```

### StateFlow in ViewModel

```kotlin
class SongViewModel(
    private val getSongDetailUseCase: GetSongDetailUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SongUiState>(SongUiState.Loading)
    val uiState: StateFlow<SongUiState> = _uiState.asStateFlow()
    
    fun loadSong(id: Long) {
        viewModelScope.launch {
            getSongDetailUseCase(id)
                .collect { song ->
                    _uiState.value = SongUiState.Success(song)
                }
        }
    }
}
```

## Module Structure

```
pws-core/
│
├── domain/                      # Application core
│   │   ├── song/
│   │   │   ├── model/           # SongDetail, SongSummary, Lyric, etc.
│   │   │   ├── repository/      # SongReadRepository, SongWriteRepository, SongObserveRepository
│   │   │   ├── usecase/         # GetSongDetailUseCase, SearchSongsUseCase, etc.
│   │   │   ├── command/         # CreateSongCommand, UpdateSongCommand
│   │   │   └── query/           # SongQuery, SearchQuery, SongSort
│   │   ├── book/
│   │   ├── bookstatistic/
│   │   ├── songnumber/
│   │   ├── tag/
│   │   ├── songtag/
│   │   ├── favorite/
│   │   ├── history/
│   │   ├── cross/               # Cross-module projections
│   │   ├── songreference/
│   │   ├── auth/
│   │   ├── payment/
│   │   ├── person/
│   │   ├── tonality/
│   │   └── core/                # ids/, pagination/, result/, transaction/, value objects
│   ├── domain-test-fixtures/    # Test data generators (Kotest Arb)
│   └── lyric-format/            # Song lyrics parsing (Kudzu parser)
│
├── api/
│   ├── contract/                # API DTOs (Serializable), Api resources
│   ├── client/                  # Ktor client
│   │   └── repository/          # Remote repositories
│   └── mapping/                 # DTO ↔ Domain mapping
│
├── features/                    # UI Layer
│   ├── app/                     # App-wide components
│   ├── search/                  # Search
│   ├── song/                    # Song screen
│   ├── book/books/              # Songbooks
│   ├── favorites/               # Favorites
│   ├── history/                 # History
│   ├── tags/                    # Tags
│   ├── components/              # Reusable components
│   ├── theme/                   # Theme and styles
│   └── di/                      # Koin modules
│
├── core/
│   ├── navigation/              # Navigation screens
│   └── ui/                      # Shared UI components
│
├── data/
│   ├── db-room/                 # Room Database
│   └── repo-room/               # Local repositories
│
└── backup/                      # Backup/Restore
```

## Testing

### Testing Levels

| Module     | Test Type   | Tools                    |
|------------|-------------|--------------------------|
| domain     | Unit tests  | Kotest                   |
| api/client | Integration | Kotest + Ktor MockEngine |
| features   | UI tests    | Compose Test             |
| db-room    | Unit tests  | Robolectric              |

### Test Fixtures

The `domain-test-fixtures` module contains generators for test data:

```kotlin
fun Arb.Companion.songSummary(
    id: Arb<SongId> = Arb.songId(),
    version: Arb<Version> = Arb.version(),
    locale: Arb<Locale> = Arb.locale(),
    name: Arb<NonEmptyString> = Arb.nonEmptyString(1..40),
    edited: Arb<Boolean> = Arb.boolean()
): Arb<SongSummary> =
    arbitrary {
        SongSummary(id = id.bind(), version = version.bind(), locale = locale.bind(), name = name.bind(), edited = edited.bind())
    }
```

---

## Data Synchronization

Mobile applications operate in **offline-first** mode with synchronization when network becomes available.

See [SYNC.md](SYNC.md) for details.

### Key Components

- **SyncManager** — coordinates synchronization of all entities
- **PendingChanges** — queue of changes waiting to be sent
- **ConflictResolver** — conflict resolution during merge
- **ConnectivityObserver** — network state monitoring

### Synchronized Entities

| Entity         | Conflict Strategy       |
|----------------|-------------------------|
| Favorites      | Last-Write-Wins         |
| History        | Merge (append-only)     |
| User Tags      | Last-Write-Wins + Merge |
| User Overrides | Last-Write-Wins         |

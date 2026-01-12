# PWS Core — Guide for AI Agents

> **Quick start**: Read [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md) to understand the project.

## About the Project

PWS (Praise & Worship Songs) — a multiplatform Christian songbook.

| Platform              | Data Source       | Offline |
|-----------------------|-------------------|---------|
| Android/iOS           | Local Room DB     | ✅       |
| Web/Telegram Mini App | Remote API        | ❌       |

### Related Repositories

| Repository | Purpose |
|------------|---------|
| **pws-core** (this) | Multiplatform library: domain, UI, API client |
| **pws-server** | Backend server (Ktor): REST API, Elasticsearch, PostgreSQL |
| **pws-android** | Android application (uses pws-core) |

> ⚠️ When working with pws-core, keep in mind that API contracts (:api:contract) must match pws-server.

## Documentation

| File                                               | Contents                    |
|----------------------------------------------------|-----------------------------|
| [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md)           | Brief project context       |
| [docs/ai/CONTRIBUTING.md](docs/ai/CONTRIBUTING.md) | Development guidelines      |
| [docs/GLOSSARY.md](docs/GLOSSARY.md)               | Glossary of terms           |
| [docs/FEATURES.md](docs/FEATURES.md)               | Feature descriptions        |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)       | Application architecture    |
| [docs/MODULES.md](docs/MODULES.md)                 | Module descriptions         |
| [docs/DATA_FLOW.md](docs/DATA_FLOW.md)             | API and data flows          |
| [docs/SYNC.md](docs/SYNC.md)                       | Data synchronization (mobile) |
| [docs/features/\*.md](docs/features/)              | Detailed feature documentation |

## Quick Code Navigation

### Domain Models

```
domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/model/
```

### Use Cases

```
domain/src/commonMain/kotlin/io/github/alelk/pws/domain/{entity}/usecase/
```

### UI Screens

```
features/src/commonMain/kotlin/io/github/alelk/pws/features/{feature}/
```

### Remote Repositories

```
api/client/src/commonMain/kotlin/repository/
```

## Key Patterns

```kotlin
// Use Case
class GetSongDetailUseCase(private val repo: SongReadRepository) {
    suspend operator fun invoke(id: Long): SongDetail?
}

// Repository Interface (domain)
interface SongReadRepository {
    suspend fun getSong(id: Long): SongDetail?
    fun observeSong(id: Long): Flow<SongDetail?>
}

// ViewModel
class SongViewModel(private val useCase: GetSongDetailUseCase) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
}
```

## Technologies

- **Kotlin Multiplatform**
- **Compose Multiplatform** (UI)
- **Voyager** (navigation)
- **Koin** (DI)
- **Ktor** (HTTP)
- **Room** (local DB)
- **Kotest** (testing)

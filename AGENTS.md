# PWS Core — Руководство для AI-агентов

> **Быстрый старт**: Читай [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md) для понимания проекта.

## О проекте

PWS (Praise & Worship Songs) — мультиплатформенный христианский песенник.

| Платформа             | Источник данных   | Оффлайн |
|-----------------------|-------------------|---------|
| Android/iOS           | Локальная Room DB | ✅       |
| Web/Telegram Mini App | Remote API        | ❌       |

### Связанные репозитории

| Репозиторий | Назначение |
|-------------|------------|
| **pws-core** (этот) | Мультиплатформенная библиотека: domain, UI, API client |
| **pws-server** | Backend сервер (Ktor): REST API, Elasticsearch, PostgreSQL |
| **pws-android** | Android приложение (использует pws-core) |

> ⚠️ При работе с pws-core учитывай, что API контракты (:api:contract) должны соответствовать pws-server.

## Документация

| Файл                                               | Содержимое                |
|----------------------------------------------------|---------------------------|
| [docs/ai/CONTEXT.md](docs/ai/CONTEXT.md)           | Краткий контекст проекта  |
| [docs/ai/CONTRIBUTING.md](docs/ai/CONTRIBUTING.md) | Инструкции для разработки |
| [docs/GLOSSARY.md](docs/GLOSSARY.md)               | Глоссарий терминов        |
| [docs/FEATURES.md](docs/FEATURES.md)               | Описание функционала      |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)       | Архитектура приложения    |
| [docs/MODULES.md](docs/MODULES.md)                 | Описание модулей          |
| [docs/DATA_FLOW.md](docs/DATA_FLOW.md)             | API и потоки данных       |
| [docs/features/\*.md](docs/features/)              | Детальное описание фич    |

## Быстрая навигация по коду

### Domain модели

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

## Ключевые паттерны

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

## Технологии

- **Kotlin Multiplatform**
- **Compose Multiplatform** (UI)
- **Voyager** (навигация)
- **Koin** (DI)
- **Ktor** (HTTP)
- **Room** (локальная БД)
- **Kotest** (тесты)


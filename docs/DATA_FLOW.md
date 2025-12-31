# Потоки данных и API

## Обзор

PWS Core поддерживает два источника данных, выбираемых на уровне DI:

| Платформа         | Data Source     | Модуль           |
|-------------------|-----------------|------------------|
| Android/iOS       | Room Database   | `:data:repo-room` |
| Web/TG Mini App   | Remote API      | `:api:client`     |

## Архитектура потоков данных

```
┌─────────────────────────────────────────────────────────────┐
│                     UI Layer                                 │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    ViewModel                         │    │
│  │  ┌───────────┐  ┌────────────┐  ┌────────────────┐  │    │
│  │  │ StateFlow │  │ User Input │  │ LaunchedEffect │  │    │
│  │  └─────┬─────┘  └─────┬──────┘  └───────┬────────┘  │    │
│  │        │              │                 │           │    │
│  └────────┼──────────────┼─────────────────┼───────────┘    │
└───────────┼──────────────┼─────────────────┼────────────────┘
            │              │                 │
            ▼              ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   Domain Layer                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    Use Cases                         │    │
│  │  ┌─────────────┐  ┌───────────┐  ┌────────────────┐ │    │
│  │  │ GetSongUC   │  │ SearchUC  │  │ AddFavoriteUC  │ │    │
│  │  └──────┬──────┘  └─────┬─────┘  └───────┬────────┘ │    │
│  │         │               │                │          │    │
│  └─────────┼───────────────┼────────────────┼──────────┘    │
└────────────┼───────────────┼────────────────┼───────────────┘
             │               │                │
             ▼               ▼                ▼
┌─────────────────────────────────────────────────────────────┐
│               Repository Interfaces                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  SongReadRepository  │  SearchRepository  │  ...    │    │
│  └─────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          ▼                                     ▼
┌─────────────────────────┐   ┌───────────────────────────────┐
│   Local Implementation  │   │    Remote Implementation      │
│    (:data:repo-room)    │   │       (:api:client)           │
│  ┌───────────────────┐  │   │  ┌─────────────────────────┐  │
│  │ RoomSongRepository│  │   │  │ RemoteSongRepository    │  │
│  │        ↓          │  │   │  │          ↓              │  │
│  │   Room DAO        │  │   │  │    Ktor Client          │  │
│  │        ↓          │  │   │  │          ↓              │  │
│  │   SQLite DB       │  │   │  │    PWS Server API       │  │
│  └───────────────────┘  │   │  └─────────────────────────┘  │
│                         │   │                               │
│  📱 Android/iOS         │   │  🌐 Web / TG Mini App         │
└─────────────────────────┘   └───────────────────────────────┘
```

## Реактивный поток данных (Flow)

### Подписка на данные

```kotlin
// Observe репозиторий возвращает Flow
interface SongObserveRepository {
    fun observe(id: SongId): Flow<SongDetail?>
}

// ViewModel подписывается
class SongViewModel(observeSong: ObserveSongUseCase) : ViewModel() {
    val song: StateFlow<SongDetail?> = observeSong(songId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

// UI подписывается на StateFlow
@Composable
fun SongScreen() {
    val song by viewModel.song.collectAsState()
}
```

### Обновление данных

```
User Action (Add Favorite)
       │
       ▼
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  ViewModel  │ ──▶ │  Use Case   │ ──▶ │  Repository  │
└─────────────┘     └─────────────┘     └──────────────┘
                                               │
                                               ▼
                                        ┌──────────────┐
                                        │   DB / API   │
                                        └──────────────┘
                                               │
       ┌───────────────────────────────────────┘
       │ Flow emits new value
       ▼
┌─────────────┐     ┌─────────────────┐
│  ViewModel  │ ──▶ │  UI Recomposes  │
│ (StateFlow) │     │  automatically  │
└─────────────┘     └─────────────────┘
```

## API Endpoints (Remote)

PWS Server предоставляет REST API с разделением на три категории. Клиент использует Ktor с Resources.

### Глобальные endpoints (read-only)

В целевой картине — публичные данные, доступные без авторизации или с опциональной авторизацией.
Но в текущей версии, пока не реализовано приложение web, авторизация обязательна.

| Метод  | Endpoint                      | Описание                                           |
|--------|-------------------------------|----------------------------------------------------|
| GET    | `/v1/books`                   | Список сборников (поиск по фильтрам с сортировкой) |
| GET    | `/v1/books/{id}`              | Детали сборника                                    |
| GET    | `/v1/books/{id}/songs`        | Песни сборника                                     |
| GET    | `/v1/songs`                   | Список песен (поиск по фильтрам с сортировкой)     |
| GET    | `/v1/songs/{id}`              | Детали песни                                       |
| GET    | `/v1/tags`                    | Список тегов                                       |
| GET    | `/v1/tags/{id}`               | Детали тега                                        |
| GET    | `/v1/tags/{id}/songs`         | Песни по тегу                                      |

### Пользовательские endpoints (read-write)

Данные авторизованного пользователя, требуют Bearer токен:

| Метод  | Endpoint                      | Описание                                                                                                                       |
|--------|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/v1/user/books`              | Сборники (глобальные + пользовательские). Поддерживается поиск по фильтрам с сортировкой.                                      |
| POST   | `/v1/user/books`              | Добавить пользовательский сборник.                                                                                             |
| PUT    | `/v1/user/books/{id}`         | Изменить сборник. Доступно изменение только пользовательских сборников. Глобальный сборник редактировать нельзя.               |
| GET    | `/v1/user/favorites`          | Избранное пользователя                                                                                                         |
| PUT    | `/v1/user/favorites/{songId}` | Добавить в избранное                                                                                                           |
| DELETE | `/v1/user/favorites/{songId}` | Удалить из избранного                                                                                                          |
| GET    | `/v1/user/history`            | История просмотров                                                                                                             |
| GET    | `/v1/user/tags`               | Теги (глобальные + пользовательские).                                                                                          |
| POST   | `/v1/user/tags`               | Создать пользовательский тег                                                                                                   |
| PUT    | `/v1/user/tags/{id}`          | Обновить тег. Если тег глобальный, то создается пользовательский override. Сам глобальный тег не меняется.                     |
| DELETE | `/v1/user/tags/{id}`          | Удалить тег. Если тег глобальный, то создается пользовательский override (тег помечается удаленным для текущего пользователя). |

### Административные endpoints (read-write)

Управление глобальными данными, требуют роль администратора:

| Метод  | Endpoint                      | Описание                        |
|--------|-------------------------------|---------------------------------|
| GET    | `/v1/admin/books`             | Список всех сборников           |
| POST   | `/v1/admin/books`             | Создать сборник                 |
| PUT    | `/v1/admin/books/{id}`        | Обновить сборник                |
| DELETE | `/v1/admin/books/{id}`        | Удалить сборник                 |
| GET    | `/v1/admin/songs`             | Список всех песен               |
| POST   | `/v1/admin/songs`             | Создать песню                   |
| PUT    | `/v1/admin/songs/{id}`        | Обновить песню                  |
| DELETE | `/v1/admin/songs/{id}`        | Удалить песню                   |
| GET    | `/v1/admin/tags`              | Список всех тегов               |
| POST   | `/v1/admin/tags`              | Создать тег                     |
| PUT    | `/v1/admin/tags/{id}`         | Обновить тег                    |
| DELETE | `/v1/admin/tags/{id}`         | Удалить тег                     |

### Авторизация (Web/TG Mini App)

```
┌─────────────────────────────────────────────────────────┐
│  Telegram Mini App                                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │  initData (from Telegram WebApp)                  │  │
│  └──────────────────────┬────────────────────────────┘  │
└─────────────────────────┼───────────────────────────────┘
                          │
                          ▼ Authorization: TgWebApp {initData}
┌─────────────────────────────────────────────────────────┐
│  PWS Server                                             │
│  ┌───────────────────────────────────────────────────┐  │
│  │  1. Validate initData signature                   │  │
│  │  2. Extract user_id                               │  │
│  │  3. Process request with user context             │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Маппинг данных

### DTO ↔ Domain

```
┌─────────────────────┐     ┌─────────────────────┐
│   API DTO           │     │   Domain Model      │
│   (:api:contract)   │     │   (:domain)         │
│                     │     │                     │
│  SongDto            │ ──▶ │  SongDetail         │
│  BookDto            │ ──▶ │  Book               │
│  TagDto             │ ──▶ │  Tag                │
└─────────────────────┘     └─────────────────────┘
          │                          ▲
          │                          │
          └──────────────────────────┘
                :api:mapping
              toDto() / toDomain()
```

### Пример маппинга

```kotlin
// api/mapping/.../SongMapping.kt
fun SongDto.toDomain(): SongDetail = SongDetail(
    id = id,
    title = title,
    lyric = lyric,
    // ...
)

fun SongDetail.toDto(): SongDto = SongDto(
    id = id,
    title = title,
    lyric = lyric,
    // ...
)
```

## Локальное хранение (Room)

### Entity ↔ Domain

```
┌─────────────────────┐     ┌─────────────────────┐
│   Room Entity       │     │   Domain Model      │
│   (:data:db-room)   │     │   (:domain)         │
│                     │     │                     │
│  SongEntity         │ ──▶ │  SongDetail         │
│  BookEntity         │ ──▶ │  Book               │
│  FavoriteEntity     │ ──▶ │  Favorite           │
└─────────────────────┘     └─────────────────────┘
          │                          ▲
          │                          │
          └──────────────────────────┘
               :data:repo-room
              (внутренний маппинг)
```

## Dependency Injection (Koin)

### Конфигурация по платформе

```kotlin
// Android/iOS приложение
val appModule = module {
    // Локальные репозитории
    single<SongReadRepository> { RoomSongReadRepository(get()) }
    single<FavoriteWriteRepository> { RoomFavoriteWriteRepository(get()) }
    // ...
}

// Web/TG Mini App
val appModule = module {
    // Remote репозитории
    single<SongReadRepository> { RemoteSongReadRepository(get()) }
    single<FavoriteWriteRepository> { RemoteFavoriteWriteRepository(get()) }
    // ...
}

// Use Cases — одинаковые для всех платформ
val domainModule = module {
    factory { GetSongDetailUseCase(get()) }
    factory { AddFavoriteUseCase(get()) }
    // ...
}
```

## Error Handling

// не реализовано

## Кеширование (Future)

Для Web/TG Mini App планируется кеширование:

```
┌─────────────────────────────────────────────────────────┐
│  Request                                                │
│     │                                                   │
│     ▼                                                   │
│  ┌─────────────┐   Hit    ┌─────────────┐              │
│  │    Cache    │ ───────▶ │   Return    │              │
│  └─────────────┘          │   cached    │              │
│         │ Miss            └─────────────┘              │
│         ▼                                              │
│  ┌─────────────┐          ┌─────────────┐              │
│  │   Network   │ ───────▶ │   Update    │              │
│  │   Request   │          │   Cache     │              │
│  └─────────────┘          └─────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```


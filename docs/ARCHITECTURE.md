# Архитектура PWS Core

## Обзор

PWS Core использует **Clean Architecture** с четким разделением на слои:

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

Приложение использует Koin для DI. Репозитории подключаются в зависимости от платформы:

```kotlin
// Для Android/iOS (локальная БД)
val localModule = module {
    single<SongReadRepository> { RoomSongReadRepository(get()) }
}

// Для Web/TG Mini App (remote API)
val remoteModule = module {
    single<SongReadRepository> { RemoteSongReadRepository(get()) }
}
```

Use Cases получают репозитории через DI и не знают о реализации.

## Потоки данных

### Чтение данных (Query)

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

### Запись данных (Command)

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

## Навигация (Voyager)

Используется Voyager для multiplatform навигации.

### SharedScreens

Определены в `core/navigation`:

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

Каждый screen в `features` модуле:

```kotlin
class SongScreen(private val songId: Long) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<SongViewModel>()
        // ...
    }
}
```

## Реактивность

### Flow для данных

Репозитории возвращают `Flow` для реактивного обновления:

```kotlin
interface SongReadRepository {
    fun observeSong(id: Long): Flow<SongDetail?>
    suspend fun getSong(id: Long): SongDetail?
}
```

### StateFlow в ViewModel

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

## Модульная структура

```
pws-core/
│
├── domain/                      # Ядро приложения
│   ├── model/                   # Domain модели
│   ├── repository/              # Repository interfaces
│   ├── usecase/                 # Business logic
│   └── domain-test-fixtures/    # Тестовые данные
│
├── api/
│   ├── contract/                # API DTO (Serializable)
│   ├── client/                  # Ktor клиент
│   │   └── repository/          # Remote репозитории
│   └── mapping/                 # DTO ↔ Domain маппинг
│
├── features/                    # UI Layer
│   ├── app/                     # App-wide компоненты
│   ├── search/                  # Поиск
│   ├── song/                    # Экран песни
│   ├── book/books/              # Сборники
│   ├── favorites/               # Избранное
│   ├── history/                 # История
│   ├── tags/                    # Теги
│   ├── components/              # Переиспользуемые компоненты
│   ├── theme/                   # Тема и стили
│   └── di/                      # Koin модули
│
├── core/
│   └── navigation/              # Навигационные экраны
│
├── data/
│   ├── db-room/                 # Room Database
│   └── repo-room/               # Local репозитории
│
└── backup/                      # Backup/Restore
```

## Тестирование

### Уровни тестирования

| Модуль     | Тип тестов  | Инструменты              |
|------------|-------------|--------------------------|
| domain     | Unit tests  | Kotest                   |
| api/client | Integration | Kotest + Ktor MockEngine |
| features   | UI tests    | Compose Test             |
| db-room    | Unit tests  | Robolectric              |

### Тестовые фикстуры

Модуль `domain-test-fixtures` содержит builders для тестовых данных:

```kotlin
// Использование в тестах
val testSong = SongDetailBuilder()
    .withId(1L)
    .withTitle("Test Song")
    .build()
```


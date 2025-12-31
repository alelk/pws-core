# Инструкции для AI-агентов

## Быстрый старт

При работе с проектом:
1. Сначала прочитай [CONTEXT.md](./CONTEXT.md)
2. Для понимания функционала — [FEATURES.md](../FEATURES.md)
3. Для понимания структуры — [MODULES.md](../MODULES.md)
4. Для понимания архитектуры — [ARCHITECTURE.md](../ARCHITECTURE.md)

## Принципы разработки

### Где размещать код

| Тип кода               | Модуль                 | Пакет                                            |
|------------------------|------------------------|--------------------------------------------------|
| Domain модели          | `:domain`              | `io.github.alelk.pws.domain.{entity}.model`      |
| Repository interface   | `:domain`              | `io.github.alelk.pws.domain.{entity}.repository` |
| Use Case               | `:domain`              | `io.github.alelk.pws.domain.{entity}.usecase`    |
| Парсинг лирики         | `:domain:lyric-format` | `io.github.alelk.pws.domain.lyric.format`        |
| Remote Repository impl | `:api:client`          | `repository`                                     |
| Local Repository impl  | `:data:repo-room`      | -                                                |
| UI Screen              | `:features`            | `io.github.alelk.pws.features.{feature}`         |
| ViewModel              | `:features`            | `io.github.alelk.pws.features.{feature}`         |
| Low-level UI компонент | `:core:ui`             | `io.github.alelk.pws.core.ui`                    |

### Naming Conventions

```kotlin
// Use Cases
class GetSongDetailUseCase      // Get = получить одну запись
class GetSongsUseCase           // множественное число = список
class SearchSongsUseCase        // Search = поиск
class CreateSongUseCase         // Create = создание
class UpdateSongUseCase         // Update = обновление
class DeleteSongUseCase         // Delete = удаление
class AddFavoriteUseCase        // Add = добавить связь
class RemoveFavoriteUseCase     // Remove = удалить связь
class ObserveSongUseCase        // Observe = подписка на Flow

// Repositories
interface SongReadRepository    // Read = только чтение
interface SongObserveRepository // Read = только чтение (Flow)
interface SongWriteRepository   // Write = запись (CRUD)

// Remote implementations
class RemoteSongReadRepository
class RemoteSongWriteRepository

// ViewModels
class SongViewModel
class SearchViewModel
class FavoritesViewModel

// Screens (Voyager)
class SongScreen : Screen
class SearchScreen : Screen

// UI States
sealed interface SongUiState {
    object Loading : SongUiState
    data class Success(val song: SongDetail) : SongUiState
    data class Error(val message: String) : SongUiState
}
```

### Паттерн Use Case

```kotlin
class GetSongDetailUseCase(
    private val songRepository: SongReadRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(songId: Long): SongDetail? =
        txRunner.inRoTransaction { readRepository.get(id) }
}

// Или с Flow
class ObserveSongUseCase(
    private val songRepository: SongObserveRepository
) {
    operator fun invoke(songId: Long): Flow<SongDetail?> = songRepository.observeSong(songId)
}
```

Транзакции на уровне use cases, а не репозиториев.

### Паттерн Repository

```kotlin
// Interface в domain
interface SongReadRepository {
    suspend fun get(id: SongId): SongDetail?
    suspend fun getMany(query: SongQuery = SongQuery.Empty, sort: SongSort = SongSort.ById): List<SongSummary>
}

interface SongObserveRepository {
    fun observe(id: SongId): Flow<SongDetail?>
}

interface SongWriteRepository {
    suspend fun create(command: CreateSongCommand): CreateResourceResult<SongId>
    suspend fun update(command: UpdateSongCommand): UpdateResourceResult<SongId>
    suspend fun delete(id: SongId): DeleteResourceResult<SongId>
}
```

### Паттерн ViewModel

```kotlin
class SongDetailScreenModel(
    val songNumberId: SongNumberId,
    private val observeSong: ObserveSongUseCase
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

    init {
        screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
            observeSong(songNumberId.songId).collectLatest { detail: SongDetail? ->
                mutableState.value = detail?.let { SongDetailUiState.Content(it) } ?: SongDetailUiState.Error
            }
        }
    }
}
```

### Паттерн Screen (Voyager)

```kotlin
class SongDetailScreen(val songNumberId: SongNumberId) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SongDetailScreenModel>(parameters = { parametersOf(songNumberId) })
        val state by viewModel.state.collectAsState()
        SongDetailContent(state = state)
    }
}
```

## Тестирование

### Структура тестов

```kotlin
// Kotest Spec
class GetSongDetailUseCaseTest : FunSpec({

    val mockRepository = mockk<SongReadRepository>()
    val useCase = GetSongDetailUseCase(mockRepository)

    test("should return song when exists") {
        val song = SongDetailBuilder().build()
        coEvery { mockRepository.getSong(1L) } returns song

        val result = useCase(1L)

        result shouldBe song
    }
})
```

### Запуск тестов

```bash
# Domain tests
./gradlew :domain:jvmTest

# Lyric format tests
./gradlew :domain:lyric-format:jvmTest

# API client tests
./gradlew :api:client:jvmTest

# Backup tests
./gradlew :backup:jvmTest

# Room database tests
./gradlew :data:db-room:testDebugUnitTest :data:db-room:jvmTest

# All tests
./gradlew test
```

## Частые задачи

### Добавить новый Use Case

1. Создай класс в `domain/.../usecase/`
2. Добавь в Koin модуль в `:features` или `:api:client:di`
3. Напиши тесты в `domain/.../commonTest/`

### Добавить новый экран

1. Создай `{Feature}Screen.kt` в `features/.../`
2. Создай `{Feature}ViewModel.kt`
3. Создай `{Feature}UiState.kt`
4. Добавь ViewModel в Koin модуль
5. Добавь navigation в `SharedScreens.kt` если нужно

### Добавить новую модель

1. Создай data class в `domain/.../model/`
2. Если нужен API — создай DTO в `api/contract/`
3. Добавь маппинг в `api/mapping/`

## Не делай

- ❌ Не добавляй platform-specific код в `:domain`
- ❌ Не создавай прямых зависимостей между features
- ❌ Не используй Android/iOS imports в common модулях
- ❌ Не храни UI state в Domain моделях
- ❌ Не вызывай репозитории напрямую из UI — используй Use Cases


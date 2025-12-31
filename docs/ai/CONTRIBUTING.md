# Инструкции для AI-агентов

## Быстрый старт

При работе с проектом:
1. Сначала прочитай `docs/ai/CONTEXT.md`
2. Для понимания функционала — `docs/FEATURES.md`
3. Для понимания структуры — `docs/MODULES.md`
4. Для понимания архитектуры — `docs/ARCHITECTURE.md`

## Принципы разработки

### Где размещать код

| Тип кода | Модуль | Пакет |
|----------|--------|-------|
| Domain модели | `:domain` | `io.github.alelk.pws.domain.{entity}.model` |
| Repository interface | `:domain` | `io.github.alelk.pws.domain.{entity}.repository` |
| Use Case | `:domain` | `io.github.alelk.pws.domain.{entity}.usecase` |
| Парсинг лирики | `:domain:lyric-format` | `io.github.alelk.pws.domain.lyric.format` |
| Remote Repository impl | `:api:client` | `repository` |
| Local Repository impl | `:data:repo-room` | - |
| UI Screen | `:features` | `io.github.alelk.pws.features.{feature}` |
| ViewModel | `:features` | `io.github.alelk.pws.features.{feature}` |
| Shared UI компонент | `:features` | `io.github.alelk.pws.features.components` |
| Low-level UI компонент | `:core:ui` | `io.github.alelk.pws.core.ui` |

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
    private val songRepository: SongReadRepository
) {
    suspend operator fun invoke(songId: Long): SongDetail? {
        return songRepository.getSong(songId)
    }
}

// Или с Flow
class ObserveSongUseCase(
    private val songRepository: SongReadRepository
) {
    operator fun invoke(songId: Long): Flow<SongDetail?> {
        return songRepository.observeSong(songId)
    }
}
```

### Паттерн Repository

```kotlin
// Interface в domain
interface SongReadRepository {
    suspend fun getSong(id: Long): SongDetail?
    fun observeSong(id: Long): Flow<SongDetail?>
    suspend fun getSongs(page: Int, size: Int): List<SongSummary>
}

interface SongWriteRepository {
    suspend fun createSong(command: CreateSongCommand): Song
    suspend fun updateSong(command: UpdateSongCommand): Song
    suspend fun deleteSong(id: Long)
}
```

### Паттерн ViewModel

```kotlin
class SongViewModel(
    private val getSongDetailUseCase: GetSongDetailUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SongUiState>(SongUiState.Loading)
    val uiState: StateFlow<SongUiState> = _uiState.asStateFlow()
    
    fun loadSong(songId: Long) {
        viewModelScope.launch {
            try {
                val song = getSongDetailUseCase(songId)
                _uiState.value = if (song != null) {
                    SongUiState.Success(song)
                } else {
                    SongUiState.Error("Песня не найдена")
                }
            } catch (e: Exception) {
                _uiState.value = SongUiState.Error(e.message ?: "Ошибка")
            }
        }
    }
}
```

### Паттерн Screen (Voyager)

```kotlin
data class SongScreen(private val songId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<SongViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        
        LaunchedEffect(songId) {
            viewModel.loadSong(songId)
        }
        
        when (val state = uiState) {
            is SongUiState.Loading -> LoadingIndicator()
            is SongUiState.Success -> SongContent(state.song)
            is SongUiState.Error -> ErrorMessage(state.message)
        }
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
        // given
        val song = SongDetailBuilder().build()
        coEvery { mockRepository.getSong(1L) } returns song
        
        // when
        val result = useCase(1L)
        
        // then
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


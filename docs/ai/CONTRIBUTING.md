# Instructions for AI Agents

## Quick Start

When working with the project:
1. First read [CONTEXT.md](./CONTEXT.md)
2. For understanding functionality — [FEATURES.md](../FEATURES.md)
3. For understanding structure — [MODULES.md](../MODULES.md)
4. For understanding architecture — [ARCHITECTURE.md](../ARCHITECTURE.md)

## Development Principles

### Where to Place Code

| Code Type              | Module                 | Package                                          |
|------------------------|------------------------|--------------------------------------------------|
| Domain models          | `:domain`              | `io.github.alelk.pws.domain.{entity}.model`      |
| Repository interface   | `:domain`              | `io.github.alelk.pws.domain.{entity}.repository` |
| Use Case               | `:domain`              | `io.github.alelk.pws.domain.{entity}.usecase`    |
| Lyrics parsing         | `:domain:lyric-format` | `io.github.alelk.pws.domain.lyric.format`        |
| Remote Repository impl | `:api:client`          | `repository`                                     |
| Local Repository impl  | `:data:repo-room`      | -                                                |
| UI Screen              | `:features`            | `io.github.alelk.pws.features.{feature}`         |
| ViewModel              | `:features`            | `io.github.alelk.pws.features.{feature}`         |
| Low-level UI component | `:core:ui`             | `io.github.alelk.pws.core.ui`                    |

### Naming Conventions

```kotlin
// Use Cases
class GetSongDetailUseCase      // Get = get single record
class GetSongsUseCase           // plural = list
class SearchSongsUseCase        // Search = search
class CreateSongUseCase         // Create = creation
class UpdateSongUseCase         // Update = update
class DeleteSongUseCase         // Delete = deletion
class AddFavoriteUseCase        // Add = add relation
class RemoveFavoriteUseCase     // Remove = remove relation
class ObserveSongUseCase        // Observe = Flow subscription

// Repositories
interface SongReadRepository    // Read = read only
interface SongObserveRepository // Read = read only (Flow)
interface SongWriteRepository   // Write = write (CRUD)

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

### Use Case Pattern

```kotlin
class GetSongDetailUseCase(
    private val songRepository: SongReadRepository,
    private val txRunner: TransactionRunner
) {
    suspend operator fun invoke(songId: Long): SongDetail? =
        txRunner.inRoTransaction { readRepository.get(id) }
}

// Or with Flow
class ObserveSongUseCase(
    private val songRepository: SongObserveRepository
) {
    operator fun invoke(songId: Long): Flow<SongDetail?> = songRepository.observeSong(songId)
}
```

Transactions at use case level, not repository level.

### Repository Pattern

```kotlin
// Interface in domain
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

### Sealed Result Types

Write operations return sealed results (not exceptions):

```kotlin
sealed interface CreateResourceResult<out R : Any> {
  data class Success<out R : Any>(val resource: R) : CreateResourceResult<R>
  data class AlreadyExists<out R : Any>(val resource: R, val message: String) : Failure<R>
  data class ValidationError<out R : Any>(val resource: R, val message: String) : Failure<R>
  data class UnknownError<out R : Any>(val resource: R, val exception: Throwable?) : Failure<R>
}

// Usage:
when (val result = createSongUseCase(command)) {
  is CreateResourceResult.Success -> handleSuccess(result.resource)
  is CreateResourceResult.AlreadyExists -> showError(result.message)
  is CreateResourceResult.ValidationError -> showValidation(result.message)
  is CreateResourceResult.UnknownError -> logError(result.exception)
}
```

### OptionalField for Updates

Use `OptionalField` in update commands for patch semantics:

```kotlin
data class UpdateSongCommand(
  val id: SongId,
  val name: NonEmptyString? = null,                    // null = unchanged
  val author: OptionalField<Person?> = OptionalField.Unchanged  // Unchanged/Set/Clear
)

// Clear author:
UpdateSongCommand(id = songId, author = OptionalField.Clear)

// Set author:
UpdateSongCommand(id = songId, author = OptionalField.Set(Person("John")))
```

### ViewModel Pattern

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

### Screen Pattern (Voyager)

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

## Testing

### Test Structure

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

### Running Tests

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

## Common Tasks

### Add New Use Case

1. Create class in `domain/.../usecase/`
2. Add to Koin module in `:features` or `:api:client:di`
3. Write tests in `domain/.../commonTest/`

### Add New Screen

1. Create `{Feature}Screen.kt` in `features/.../`
2. Create `{Feature}ViewModel.kt`
3. Create `{Feature}UiState.kt`
4. Add ViewModel to Koin module
5. Add navigation to `SharedScreens.kt` if needed

### Add New Model

1. Create data class in `domain/.../model/`
2. If API needed — create DTO in `api/contract/`
3. Add mapping in `api/mapping/`

## Don't Do

- ❌ Don't add platform-specific code to `:domain`
- ❌ Don't create direct dependencies between features
- ❌ Don't use Android/iOS imports in common modules
- ❌ Don't store UI state in Domain models
- ❌ Don't call repositories directly from UI — use Use Cases
